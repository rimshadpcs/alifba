const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.hourlyLessonReminder = functions.pubsub
  .schedule("0 * * * *") // Run every hour at minute 0 (e.g., 12:00, 1:00, 2:00, etc.)
  .onRun(async () => {
    const db = admin.firestore();
    const snapshot = await db.collection("users").get();
    const messages = [];
    const now = new Date();

    // Loop through each user document
    snapshot.forEach(doc => {
      const userData = doc.data();
      const fcmToken = userData.fcmToken;
      if (!fcmToken) return;

      // Retrieve the user's time zone; default to UTC if not available.
      const timeZone = userData.timeZone || "UTC";
      // Convert the current time (UTC) to the user's local time.
      const localTime = new Date(now.toLocaleString("en-US", { timeZone }));

      // Define the notification window. For example, if you want to send notifications
      // between 7:00 PM and 8:15 PM local time, you can check:
      if ((localTime.getHours() === 19) || (localTime.getHours() === 20 && localTime.getMinutes() < 15)) {
        // Get today's date (UTC) in YYYY-MM-DD format
        const today = now.toISOString().split("T")[0];
        const lastLessonCompleted = userData.lastLessonCompleted || "";
        const streak = userData.streak || 0;
        const childProfiles = userData.childProfiles || [];
        const childName = childProfiles[0]?.childName || "friend";

        // If the user hasn't completed today's lesson, queue notifications.
        if (lastLessonCompleted !== today) {
          messages.push({
            token: fcmToken,
            notification: {
              title: "📖 Time for today's lesson!",
              body: `Keep learning, ${childName}! Your next lesson is waiting. 🚀`
            },
            android: { priority: "high" }
          });

          // If the user has a streak, send an additional reminder.
          if (streak > 0) {
            messages.push({
              token: fcmToken,
              notification: {
                title: "🔥 Don't lose your streak!",
                body: `You're on a ${streak}-day streak, ${childName}! Complete a lesson today to keep it going! 💪`
              },
              android: { priority: "high" }
            });
          }
        }
      }
    });

    if (messages.length > 0) {
      await admin.messaging().sendAll(messages);
      console.log(`Sent ${messages.length} notifications`);
    }
    return null;
  });
