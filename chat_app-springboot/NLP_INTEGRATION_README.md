# NLP Task Extraction & Google Tasks Integration

## Overview

This implementation enhances the HolySync chat application with intelligent task extraction using NLP and seamless Google Tasks integration. The system automatically detects tasks and due dates from chat messages in real-time and allows users to export them to their Google Tasks.

## Features Implemented

### 1. NLP Task Extraction
- **Lightweight NLP Library**: Uses Compromise.js (200KB, client-side) for natural language processing
- **Real-time Detection**: Automatically extracts tasks as messages are sent
- **Smart Pattern Recognition**: Identifies task keywords, action verbs, and imperative sentences
- **Due Date Parsing**: Extracts and parses natural language dates (tomorrow, next week, January 15, etc.)
- **Confidence Scoring**: Only displays tasks with reasonable confidence levels

### 2. Enhanced Task UI
- **Visual Due Date Indicators**: Color-coded badges for overdue, due soon, and future tasks
- **Task Metadata**: Shows sender, timestamp, and due date information
- **Responsive Design**: Works on desktop and mobile devices
- **Real-time Updates**: Tasks appear instantly as they're detected

### 3. Google Tasks Integration
- **OAuth 2.0 Flow**: Secure authentication with Google
- **Popup Authentication**: Clean user experience with popup window
- **Task Export**: Bulk export of detected tasks with due dates
- **Error Handling**: Comprehensive error handling and user feedback

## Technical Implementation

### Frontend Components

#### Files Modified/Created:
- `chat.html` - Enhanced with NLP integration and OAuth flow
- `js/nlp-task-extractor.js` - Core NLP processing logic

#### Key Features:
- **Task Detection**: Uses Compromise.js to parse messages and identify tasks
- **Date Parsing**: Handles relative dates (tomorrow, next week) and absolute dates
- **UI Updates**: Real-time task display with visual indicators
- **OAuth Integration**: Popup-based Google authentication flow

### Backend Components

#### Files Created:
- `GoogleOAuthConfig.java` - OAuth configuration and credential management
- `GoogleTasksService.java` - Google Tasks API integration service
- `GoogleTasksController.java` - REST API endpoints for OAuth and task export

#### Key Features:
- **OAuth Management**: Handles authorization URL generation and token exchange
- **Google Tasks API**: Creates tasks in user's Google Tasks with due dates
- **Error Handling**: Comprehensive error handling and logging
- **Security**: Secure credential management and token handling

### Configuration

#### Files Modified/Created:
- `pom.xml` - Added Google API dependencies
- `application.properties` - OAuth configuration
- `env.example` - Environment variable template

## Setup Instructions

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google Tasks API
4. Go to Credentials > Create Credentials > OAuth 2.0 Client IDs
5. Set Application type to "Web application"
6. Add Authorized redirect URI: `http://localhost:8080/api/google-tasks/callback`
7. Copy Client ID and Client Secret

### 2. Environment Configuration

1. Copy `env.example` to `.env` (or set environment variables)
2. Set your Google OAuth credentials:
   ```
   GOOGLE_CLIENT_ID=your_client_id_here
   GOOGLE_CLIENT_SECRET=your_client_secret_here
   ```

### 3. Database Setup

Run the database schema:
```bash
psql -U chat_user -d chat_app -f database_schema.sql
```

### 4. Application Startup

```bash
cd HolySync/chat_app-springboot
./mvnw spring-boot:run
```

## Usage

### Task Detection

The system automatically detects tasks from messages containing:

**Task Keywords**: assignment, exam, test, quiz, homework, project, presentation, deadline, submit, complete, finish, review, study, prepare, meeting, appointment, task, todo, reminder, due, deliverable

**Action Verbs**: do, make, write, read, study, prepare, send, create, build, design, implement, fix, update, submit, complete, finish, review, analyze, research

**Example Messages**:
- "Remember to submit assignment by tomorrow 5pm"
- "Don't forget to study for the exam next week"
- "Need to complete the project by Friday"
- "Meeting scheduled for Monday at 2pm"

### Due Date Recognition

The system recognizes various date formats:

**Relative Dates**: tomorrow, today, next week, next month, in 3 days, in 2 weeks

**Absolute Dates**: January 15, 15th Jan, 01/15/2025, 2025-01-15

**Day of Week**: Monday, Tuesday, Wednesday, etc.

**Time Expressions**: by 5pm, before Friday, at 2pm

### Google Tasks Export

1. Click "Add to Google Tasks" button
2. Complete OAuth authentication in popup window
3. Tasks are automatically exported with due dates
4. Success notification confirms export

## API Endpoints

### Google Tasks Integration

- `GET /api/google-tasks/auth-url` - Get OAuth authorization URL
- `POST /api/google-tasks/callback` - Handle OAuth callback
- `POST /api/google-tasks/export` - Export tasks to Google Tasks
- `GET /api/google-tasks/health` - Health check endpoint

## Task Data Structure

```javascript
{
    content: "Task description",
    sender: "username",
    timestamp: "time detected",
    dueDate: "parsed due date or null",
    dueDateString: "original date text",
    confidence: 0.5 // confidence score 0-1
}
```

## Visual Indicators

- **Overdue Tasks**: Red badge with warning icon
- **Due Soon** (within 3 days): Yellow badge with clock icon
- **Future Tasks**: Blue badge with calendar icon

## Error Handling

- **NLP Errors**: Graceful fallback to regex patterns
- **OAuth Errors**: User-friendly error messages
- **API Errors**: Detailed error logging and user feedback
- **Network Errors**: Retry mechanisms and offline handling

## Security Features

- **OAuth 2.0**: Secure Google authentication
- **Token Management**: Secure access token handling
- **CORS Configuration**: Proper cross-origin resource sharing
- **Input Validation**: Comprehensive input sanitization

## Performance Considerations

- **Client-side NLP**: Reduces server load
- **Efficient Parsing**: Optimized date and task extraction
- **Minimal Dependencies**: Lightweight Compromise.js library
- **Caching**: OAuth tokens cached for session duration

## Future Enhancements

- **Task Categories**: Automatic task categorization
- **Priority Detection**: Extract task priority from messages
- **Recurring Tasks**: Detect and handle recurring tasks
- **Team Integration**: Share tasks across team members
- **Calendar Integration**: Sync with Google Calendar
- **Mobile App**: Native mobile application support

## Troubleshooting

### Common Issues

1. **OAuth Popup Blocked**: Ensure popup blockers are disabled
2. **Tasks Not Detected**: Check message contains task keywords or action verbs
3. **Date Parsing Errors**: Verify date format is supported
4. **Google API Errors**: Check OAuth credentials and API quotas

### Debug Mode

Enable debug logging by setting log level to DEBUG in application.properties:
```properties
logging.level.in.suratsaiteja.chatapp=DEBUG
```

## Dependencies

### Frontend
- Compromise.js 14.9.0 (NLP processing)
- Font Awesome 6.4.0 (Icons)
- SockJS & STOMP (WebSocket)

### Backend
- Google API Client 2.2.0
- Google Tasks API v1
- Google OAuth Client Jetty 1.34.1
- Spring Boot 3.1.0
- PostgreSQL 42.7.3

## License

This implementation is part of the HolySync chat application project.
