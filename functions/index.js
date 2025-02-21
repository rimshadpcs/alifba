const {onSchedule} = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
admin.initializeApp();

/**
 * Example message templates. You can store as many as you want.
 */
const lessonMessages = [
  {
    title: "Lesson Time!",
    body: "Hey {childName}, let's explore today's lesson!"
  },
  {
    title: "Ready for Your Lesson?",
    body: "Hi {childName}, a new lesson awaits you now!"
  },
  {
    title: "Don't Forget Your Lesson",
    body: "Hello {childName}, remember to keep learning today!"
  }
];

/**
 * Picks a rotating message for each user (you could do random, round-robin, etc.).
 * We'll just pick one at random. If you prefer something else
 * (based on userId, day of week, etc.), customize accordingly.
 */
function getRotatingMessage(messages, userId) {
  // For a random message:
  const randomIndex = Math.floor(Math.random() * messages.length);
  return messages[randomIndex];
}

/**
 * Main logic to send notifications to users between 7:00 PM and 7:30 PM
 * local time, unless 'forceNotification' is set to true.
 */
async function sendLessonNotifications(forceNotification = false) {
  const db = admin.firestore();
  console.log("Starting lesson notification process...");
  console.log(`Force notification mode: ${forceNotification}`);

  try {
    const now = new Date();
    console.log(`Current UTC time: ${now.toISOString()}`);

    // Get all users
    const snapshot = await db.collection("users").get();
    console.log(`Processing ${snapshot.size} users for lesson notifications`);

    if (snapshot.size === 0) {
      console.warn("⚠️ WARNING: No users found in the database!");
      return {
        success: false,
        error: "No users found in database",
        timestamp: now.toISOString()
      };
    }

    let notificationsSent = 0;
    let skippedDueToTime = 0;
    let skippedDueToMissingToken = 0;
    let failedNotifications = 0;

    for (const doc of snapshot.docs) {
      const userData = doc.data();
      const fcmToken = userData.fcmToken;
      const userTimeZone = userData.timeZone || "UTC";

      console.log(`Processing user ${doc.id}:`, {
        hasToken: !!fcmToken,
        timezone: userTimeZone,
        childProfiles: userData.childProfiles ? userData.childProfiles.length : 0
      });

      if (!fcmToken) {
        console.log(`⚠️ Skipping user ${doc.id}: No FCM token`);
        skippedDueToMissingToken++;
        continue;
      }

      try {
        // Convert current UTC time to the user's local time
        const userLocalTime = new Date(
          now.toLocaleString("en-US", { timeZone: userTimeZone })
        );
        const userHour = userLocalTime.getHours();
        const userMinute = userLocalTime.getMinutes();

        console.log(
          `User ${doc.id} local time: ${userHour}:${userMinute} (${userTimeZone})`
        );

        let shouldSendNotification = forceNotification; // If force=true, skip time checks

        // Check for lesson notification window: 7:00 PM - 7:30 PM local time
        if (!shouldSendNotification) {
          if (userHour === 19 && userMinute < 30) {
            shouldSendNotification = true;
            console.log(
              `✅ Time condition met for lesson notification: ${userHour}:${userMinute}`
            );
          } else {
            console.log(
              `❌ Time condition NOT met: Current: ${userHour}:${userMinute}, Required: 19:00-19:30`
            );
          }
        }

        if (!shouldSendNotification) {
          skippedDueToTime++;
          continue;
        }

        console.log(`🔔 Preparing to send lesson notification to user ${doc.id}`);

        const childProfiles = userData.childProfiles || [];
        const childName =
          childProfiles.length > 0 && childProfiles[0]?.childName
            ? childProfiles[0].childName
            : "friend";

        // Pick one of our example message templates
        const messageTemplate = getRotatingMessage(lessonMessages, doc.id);

        // Replace {childName} placeholder
        const title = messageTemplate.title;
        const body = messageTemplate.body.replace("{childName}", childName);

        const message = {
          token: fcmToken,
          notification: { title, body },
          android: {
            priority: "high",
            notification: {
              channelId: "lessons"
            }
          }
        };

        // Send the notification
        await admin.messaging().send(message);
        console.log(`✅ Successfully sent lesson notification to user ${doc.id}`);
        notificationsSent++;

      } catch (error) {
        failedNotifications++;
        console.error(`❌ Error sending to user ${doc.id}:`, error);

        // If invalid/expired token, remove it from Firestore
        if (
          error.code === "messaging/invalid-registration-token" ||
          error.code === "messaging/registration-token-not-registered"
        ) {
          console.log(`🔄 Removing invalid token for user ${doc.id}`);
          try {
            await db
              .collection("users")
              .doc(doc.id)
              .update({ fcmToken: admin.firestore.FieldValue.delete() });
            console.log(`✅ Successfully removed invalid token for user ${doc.id}`);
          } catch (updateError) {
            console.error(`❌ Failed to remove token: ${updateError.message}`);
          }
        }
      }
    }

    // Summary
    const result = {
      success: true,
      notificationsSent,
      usersProcessed: snapshot.size,
      skippedDueToTime,
      skippedDueToMissingToken,
      failedNotifications,
      timestamp: now.toISOString(),
      forceMode: forceNotification
    };

    console.log("📊 Notification process summary:", result);
    return result;
  } catch (error) {
    console.error("❌ Main lesson notification error:", error);
    throw error;
  }
}

// Firebase Functions v2 Scheduled function
exports.lessonReminder = onSchedule({
  schedule: "every 60 minutes",
  timeZone: "UTC",
  retryConfig: {
    maxRetryAttempts: 3,
  },
}, async (context) => {
  return sendLessonNotifications(false);
});