# Debugging & Testing Guide

## Code Review Summary ✅

I've reviewed the entire codebase and found/fixed several issues:

### Issues Found & Fixed:

1. **✅ OAuth Flow Communication**: Fixed popup-to-parent window communication
2. **✅ Redirect URI**: Updated to use static HTML callback page
3. **✅ Java Compatibility**: Fixed `.toList()` method for Java 17
4. **✅ Callback Page**: Created proper OAuth callback handler

## Current Status: READY FOR TESTING 🚀

### Files Status:
- ✅ **Backend**: All Java classes properly configured
- ✅ **Frontend**: NLP integration and OAuth flow implemented
- ✅ **Dependencies**: Google API libraries added to pom.xml
- ✅ **Configuration**: OAuth settings in application.properties
- ✅ **Environment**: .env file setup instructions provided

## Next Steps for Testing:

### 1. **Update Google Cloud Console** ⚠️
**IMPORTANT**: Update your OAuth credentials with the new redirect URI:

```
Authorized redirect URIs: http://localhost:8080/oauth-callback.html
```

### 2. **Start the Application**
```bash
cd HolySync/chat_app-springboot
./mvnw spring-boot:run
```

### 3. **Test the Complete Flow**

#### Step 1: Test NLP Task Detection
1. Open `http://localhost:8080/login.html`
2. Login with existing credentials
3. Send messages like:
   - "Remember to submit assignment by tomorrow 5pm"
   - "Don't forget to study for the exam next week"
   - "Need to complete the project by Friday"
4. **Expected**: Tasks should appear in the right sidebar with due dates

#### Step 2: Test Google Tasks Integration
1. Click "Add to Google Tasks" button
2. **Expected**: OAuth popup should open
3. Complete Google authentication
4. **Expected**: Tasks should be exported to your Google Tasks

### 4. **Debugging Commands**

#### Check Application Health:
```bash
curl http://localhost:8080/api/google-tasks/health
```

#### Test OAuth URL Generation:
```bash
curl http://localhost:8080/api/google-tasks/auth-url
```

#### Check Application Logs:
```bash
# Look for these log messages:
# - "Google OAuth configuration loaded"
# - "Google Tasks service initialized"
# - "OAuth authorization URL generated"
```

### 5. **Common Issues & Solutions**

#### Issue: "Failed to generate authorization URL"
**Solution**: Check your .env file has correct credentials:
```env
GOOGLE_CLIENT_ID=your_actual_client_id
GOOGLE_CLIENT_SECRET=your_actual_client_secret
```

#### Issue: "OAuth popup blocked"
**Solution**: 
- Disable popup blockers
- Allow popups for localhost:8080

#### Issue: "No tasks detected"
**Solution**: 
- Check browser console for NLP errors
- Ensure Compromise.js loaded properly
- Try messages with task keywords: "assignment", "exam", "deadline"

#### Issue: "Tasks not exported to Google"
**Solution**:
- Check Google Cloud Console redirect URI
- Verify Google Tasks API is enabled
- Check browser network tab for API errors

### 6. **Testing Checklist**

- [ ] Application starts without errors
- [ ] Login/signup works
- [ ] Chat messages display correctly
- [ ] NLP detects tasks from messages
- [ ] Due dates are parsed correctly
- [ ] Tasks appear in sidebar with visual indicators
- [ ] OAuth popup opens when clicking "Add to Google Tasks"
- [ ] Google authentication completes successfully
- [ ] Tasks are exported to Google Tasks
- [ ] Success/error messages display properly

### 7. **Browser Console Debugging**

Open browser DevTools (F12) and check:

#### Console Messages:
```javascript
// Should see:
"Compromise.js loaded successfully"
"Task detected: [task content]"
"OAuth popup opened"
"Tasks exported successfully"
```

#### Network Tab:
- Check `/api/google-tasks/auth-url` returns 200
- Check `/api/google-tasks/callback` returns 200
- Check `/api/google-tasks/export` returns 200

### 8. **Environment Variables Check**

Verify your `.env` file contains:
```env
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-abcdefghijklmnopqrstuvwxyz123456
```

### 9. **Database Check**

Ensure PostgreSQL is running and database is set up:
```bash
psql -U chat_user -d chat_app -c "SELECT * FROM users LIMIT 5;"
```

## Expected Behavior:

1. **Task Detection**: Messages with task keywords automatically create tasks in sidebar
2. **Due Date Parsing**: Natural language dates are converted to proper dates
3. **Visual Indicators**: Tasks show color-coded due date badges
4. **Google Integration**: One-click export to Google Tasks with OAuth flow
5. **Error Handling**: User-friendly error messages for all failure scenarios

## Ready to Test! 🎯

The implementation is complete and ready for testing. Follow the steps above to verify everything works correctly!
