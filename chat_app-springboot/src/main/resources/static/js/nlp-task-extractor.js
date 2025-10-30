/**
 * NLP Task Extractor using Compromise.js
 * Extracts tasks and due dates from chat messages
 */

// Task indicator keywords
const TASK_KEYWORDS = [
    'assignment', 'exam', 'test', 'quiz', 'homework', 'project', 
    'presentation', 'deadline', 'submit', 'complete', 'finish', 
    'review', 'study', 'prepare', 'meeting', 'appointment',
    'task', 'todo', 'reminder', 'due', 'deliverable'
];

// Action verbs that indicate tasks
const ACTION_VERBS = [
    'do', 'make', 'write', 'read', 'study', 'prepare', 'send', 
    'create', 'build', 'design', 'implement', 'fix', 'update',
    'submit', 'complete', 'finish', 'review', 'analyze', 'research',
    'call', 'email', 'pay', 'buy', 'book', 'schedule', 'plan', 'organize', 'clean', 'draft', 'deploy'
];

// Modal and prompt prefixes that often precede tasks
const TASK_PREFIX_PATTERNS = [
    /\b(remember|don\'t forget|make sure)\s+to\s+/i,
    /\b(need|have|must|should|ought)\s+to\s+/i,
    /\b(can you|could you|please)\s+/i
];

// Date/time patterns
const DATE_PATTERNS = [
    // Relative dates
    /tomorrow/i,
    /today/i,
    /next week/i,
    /next month/i,
    /in \d+ days?/i,
    /in \d+ weeks?/i,
    /in \d+ months?/i,
    
    // Absolute dates
    /\b(january|february|march|april|may|june|july|august|september|october|november|december)\s+\d{1,2}\b/i,
    /\b\d{1,2}\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\b/i,
    /\b\d{1,2}\/\d{1,2}\/\d{2,4}\b/,
    /\b\d{4}-\d{2}-\d{2}\b/,
    
    // Day of week
    /\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b/i,
    
    // Time expressions
    /\b(by|before|until|at)\s+\d{1,2}:\d{2}\s*(am|pm)?\b/i,
    /\b(by|before|until)\s+(tomorrow|today|next week|friday|monday|etc)\b/i
];

/**
 * Extract tasks from a message using NLP
 * @param {string} messageText - The message to analyze
 * @returns {Array} Array of extracted tasks with metadata
 */
function extractTasksFromMessage(messageText) {
    // If no message text, nothing to do
    if (!messageText) return [];

    // Warn if compromise is not available but still try a fallback
    if (!window.nlp) {
        console.warn('Compromise.js not loaded; using simple sentence split fallback');
    }

    const tasks = [];

    // Obtain sentences robustly: prefer compromise's sentence splitter, fallback to regex
    let sentences = [];
    try {
        if (window.nlp) {
            const doc = nlp(messageText);
            // out('array') returns an array of sentence strings
            sentences = doc.sentences().out('array');
        } else {
            sentences = messageText.split(/(?<=[.!?])\s+/);
        }
    } catch (e) {
        // Fallback if any error occurs
        sentences = messageText.split(/(?<=[.!?])\s+/);
    }

    // Normalize and filter empty sentences
    sentences = sentences.map(s => s && s.trim()).filter(Boolean);

    // Imperative detection: check if sentence starts with an action verb
    const imperativeRegex = new RegExp('^(' + ACTION_VERBS.join('|') + ')\\b', 'i');

    sentences.forEach(sentenceText => {
        const text = sentenceText.toLowerCase();

        const hasTaskKeyword = TASK_KEYWORDS.some(keyword => text.includes(keyword));
        const hasActionVerb = ACTION_VERBS.some(verb => new RegExp('\\b' + verb + '\\b', 'i').test(sentenceText));
        const isImperative = imperativeRegex.test(sentenceText);

        // Detect common "to <verb>" task phrasing (e.g., "remember to submit", "need to call")
        const hasTaskPrefix = TASK_PREFIX_PATTERNS.some(rx => rx.test(sentenceText));
        const hasToVerb = /\bto\s+([a-z]{2,})/i.test(sentenceText) && ACTION_VERBS.some(verb => new RegExp('\\bto\\s+' + verb + '\\b', 'i').test(sentenceText));

        if (hasTaskKeyword || hasActionVerb || isImperative || hasTaskPrefix || hasToVerb) {
            const task = extractTaskFromSentence(sentenceText);
            if (task) tasks.push(task);
        }
    });

    return tasks;
}

/**
 * Extract a single task from a sentence
 * @param {string} sentenceText - The sentence to analyze
 * @returns {Object|null} Task object or null if no task found
 */
function extractTaskFromSentence(sentenceText) {
    const text = sentenceText.toLowerCase();

    // Check if sentence contains task indicators
    const hasTaskKeyword = TASK_KEYWORDS.some(keyword => text.includes(keyword));
    const hasActionVerb = ACTION_VERBS.some(verb => text.includes(verb));
    
    if (!hasTaskKeyword && !hasActionVerb) {
        return null;
    }

    // Extract due date
    const dueDateInfo = extractDueDate(sentenceText);
    
    // Clean up the task description
    let taskDescription = sentenceText.trim();
    
    // Remove common prefixes
    taskDescription = taskDescription.replace(/^(remember to|don't forget to|make sure to|please|can you|could you)\s+/i, '');
    
    // Remove date/time expressions from description
    if (dueDateInfo.originalText) {
        taskDescription = taskDescription.replace(dueDateInfo.originalText, '').trim();
    }
    
    // Clean up extra spaces and punctuation
    taskDescription = taskDescription.replace(/\s+/g, ' ').replace(/[.,!?]+$/, '');

    return {
        content: taskDescription,
        dueDate: dueDateInfo.parsedDate,
        dueDateString: dueDateInfo.originalText,
        confidence: calculateConfidence(taskDescription, dueDateInfo.originalText)
    };
}

/**
 * Extract due date information from text
 * @param {string} text - Text to analyze for dates
 * @returns {Object} Date information object
 */
function extractDueDate(text) {
    let parsedDate = null;
    let originalText = null;

    try {
        if (window.nlp) {
            const doc = nlp(text);
            const dates = doc.dates();
            if (dates && dates.length > 0) {
                const date = dates.first();
                originalText = date.text();
                parsedDate = parseDueDate(originalText);
            }
        }
    } catch (e) {
        // swallow and continue to regex fallback
    }

    if (!parsedDate) {
        for (const pattern of DATE_PATTERNS) {
            const match = text.match(pattern);
            if (match) {
                originalText = match[0];
                parsedDate = parseDueDate(originalText);
                break;
            }
        }
    }

    return {
        parsedDate: parsedDate,
        originalText: originalText
    };
}

/**
 * Parse natural language date to Date object
 * @param {string} dateText - Natural language date text
 * @returns {Date|null} Parsed date or null
 */
function parseDueDate(dateText) {
    if (!dateText) return null;

    const now = new Date();
    const text = dateText.toLowerCase().trim();

    try {
        // Handle relative dates
        if (text.includes('tomorrow')) {
            const tomorrow = new Date(now);
            tomorrow.setDate(tomorrow.getDate() + 1);
            return tomorrow;
        }
        
        if (text.includes('today')) {
            return new Date(now);
        }
        
        if (text.includes('next week')) {
            const nextWeek = new Date(now);
            nextWeek.setDate(nextWeek.getDate() + 7);
            return nextWeek;
        }
        
        if (text.includes('next month')) {
            const nextMonth = new Date(now);
            nextMonth.setMonth(nextMonth.getMonth() + 1);
            return nextMonth;
        }

        // Handle "in X days"
        const daysMatch = text.match(/in (\d+) days?/);
        if (daysMatch) {
            const days = parseInt(daysMatch[1]);
            const futureDate = new Date(now);
            futureDate.setDate(futureDate.getDate() + days);
            return futureDate;
        }

        // Handle "in X weeks"
        const weeksMatch = text.match(/in (\d+) weeks?/);
        if (weeksMatch) {
            const weeks = parseInt(weeksMatch[1]);
            const futureDate = new Date(now);
            futureDate.setDate(futureDate.getDate() + (weeks * 7));
            return futureDate;
        }

        // Handle day of week
        const dayNames = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
        const dayIndex = dayNames.findIndex(day => text.includes(day));
        if (dayIndex !== -1) {
            const targetDate = new Date(now);
            const currentDay = now.getDay();
            const daysUntilTarget = (dayIndex - currentDay + 7) % 7;
            targetDate.setDate(targetDate.getDate() + daysUntilTarget);
            return targetDate;
        }

        // Handle absolute dates (let JavaScript's Date constructor handle it)
        const absoluteDate = new Date(dateText);
        if (!isNaN(absoluteDate.getTime())) {
            return absoluteDate;
        }

        // Handle time expressions like "by 5pm"
        const timeMatch = text.match(/(\d{1,2}):?(\d{2})?\s*(am|pm)?/);
        if (timeMatch) {
            const hours = parseInt(timeMatch[1]);
            const minutes = timeMatch[2] ? parseInt(timeMatch[2]) : 0;
            const ampm = timeMatch[3];
            
            let targetHours = hours;
            if (ampm === 'pm' && hours !== 12) {
                targetHours += 12;
            } else if (ampm === 'am' && hours === 12) {
                targetHours = 0;
            }
            
            const timeDate = new Date(now);
            timeDate.setHours(targetHours, minutes, 0, 0);
            
            // If time has passed today, assume it's for tomorrow
            if (timeDate <= now) {
                timeDate.setDate(timeDate.getDate() + 1);
            }
            
            return timeDate;
        }

    } catch (error) {
        console.warn('Error parsing date:', dateText, error);
    }

    return null;
}

/**
 * Calculate confidence score for task extraction
 * @param {string} taskDescription - The task description
 * @param {string} dueDateText - The due date text
 * @returns {number} Confidence score between 0 and 1
 */
function calculateConfidence(taskDescription, dueDateText) {
    let confidence = 0.5; // Base confidence
    
    // Increase confidence for task keywords
    const taskKeywordCount = TASK_KEYWORDS.filter(keyword => 
        taskDescription.toLowerCase().includes(keyword)
    ).length;
    confidence += taskKeywordCount * 0.1;
    
    // Increase confidence for action verbs
    const actionVerbCount = ACTION_VERBS.filter(verb => 
        taskDescription.toLowerCase().includes(verb)
    ).length;
    confidence += actionVerbCount * 0.05;
    
    // Increase confidence if due date is found
    if (dueDateText) {
        confidence += 0.2;
    }
    
    // Increase confidence for imperative sentences
    if (taskDescription.match(/^(do|make|write|read|study|prepare|send|create|submit|complete|finish)/i)) {
        confidence += 0.1;
    }
    
    return Math.min(confidence, 1.0);
}

/**
 * Format date for display
 * @param {Date} date - Date object
 * @returns {string} Formatted date string
 */
function formatDateForDisplay(date) {
    if (!date) return '';
    
    const now = new Date();
    const diffTime = date.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
        return 'Today';
    } else if (diffDays === 1) {
        return 'Tomorrow';
    } else if (diffDays === -1) {
        return 'Yesterday';
    } else if (diffDays < 0) {
        return `${Math.abs(diffDays)} days ago`;
    } else if (diffDays <= 7) {
        return `In ${diffDays} days`;
    } else {
        return date.toLocaleDateString();
    }
}

/**
 * Check if a task is overdue
 * @param {Date} dueDate - Due date
 * @returns {boolean} True if overdue
 */
function isOverdue(dueDate) {
    if (!dueDate) return false;
    return dueDate < new Date();
}

/**
 * Check if a task is due soon (within 3 days)
 * @param {Date} dueDate - Due date
 * @returns {boolean} True if due soon
 */
function isDueSoon(dueDate) {
    if (!dueDate) return false;
    const now = new Date();
    const threeDaysFromNow = new Date(now.getTime() + (3 * 24 * 60 * 60 * 1000));
    return dueDate <= threeDaysFromNow && dueDate >= now;
}
