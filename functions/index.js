const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendChatNotifications = functions.firestore
   .document('messages/{roomId}/chat_messages/{chat_messageId}')
   .onCreate((snap, context) => {
     // Get an object with the current document value.
     // If the document does not exist, it has been deleted.
     const document = snap.exists ? snap.data() : null;

     if (document) {
       var message = {
         topic: context.params.roomId,
         android: {
            notification: {
                title: document.from + ' sent you a message',
                body: document.text,
                click_action: 'notification',
            }
         },
         data: {
             msgSender: document.from,
         }
       };

       return admin.messaging().send(message)
         .then((response) => {
           // Response is a message ID string.
           console.log('Successfully sent message:', response);
           return response;
         })
         .catch((error) => {
           console.log('Error sending message:', error);
           return error;
         });
     }

     return "document was null or emtpy";
   });

exports.addTimeStamp = functions.firestore
   .document('messages/{roomId}/chat_messages/{chat_messageId}')
   .onCreate((snap, context) => {
     if (snap) {
       return snap.ref.update({
                   timestamp: admin.firestore.FieldValue.serverTimestamp()
               });
     }

     return "snap was null or empty";
   });
