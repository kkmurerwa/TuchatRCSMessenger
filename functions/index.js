let functions = require('firebase-functions');

let admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.firestore.document('chatrooms/{chatRoomId}/messages/{messageId}')
    .onWrite(async(snap, context) => {

        let message = snap.after.data().messageBody;
        let messageSender = snap.after.data().senderName;
        let messageUserId = snap.after.data().userId;
        let chatRoomId = context.params.chatRoomId;

        let tokens = [];
        let chatRoomRef = admin.firestore().collection("chatrooms").doc(chatRoomId);

        try {
            const transaction = await admin.firestore().runTransaction(async t => {
                const chatroom = await t.get(chatRoomRef);
                const usersArray = chatroom.data().chatMembers;

              //await not recognized inside loop since we are now in an anonymous function in the loop
                usersArray.forEach(user_id => {
                    let userIdRef = admin.firestore().collection("tokens").doc(user_id);
                   // const tokenDoc = await t.get(userIdRef);

                   return userIDRef.get().then(tokenDoc) //continue from here as we had done earlier
                  if (tokenDoc.exists) {
                        let user_token = doc.data().token;
                        functions.logger.log('token: ', user_token);
                        tokens.push(user_token);
                    }
                });
            });

            //If we are at this stage the transaction should have run successfully

            //Check if length of tokens evaluates to a truthy value (Is not empty)
            if (tokens.length) {
                functions.logger.log("Construction the notification message.");
                const payload = {

                    data: {
                        data_type: "data_type_chat_message",
                        title: "Tuchat",
                        message: message,
                        sender_id: messageUserId,
                        sender_name: messageSender,
                        chatRoom_id: chatRoomId
                    }
                };
                const options = {
                    priority: "high",
                    timeToLive: 60 * 60 * 24
                };
                const messengerResponse = await admin.messaging().sendToDevice(tokens, payload);
            } else {
                functions.logger.log('Tokens array is empty');
            }
        } catch (error) {
            //All errors that can pop up from our await statements are caught here
            functions.logger.error('Await error: ', err);
        }

        /*return admin.firestore().runTransaction(t => {
            return t.get(chatRoomRef)
                .then(chatroom => {
                    let usersArray = chatroom.data().chatMembers;
                    usersArray.forEach(user_id => {
                        let userIdRef = admin.firestore().collection("tokens").doc(user_id);
                        return t.get(userIdRef).then(doc => {
                            if (doc.exists) {
                                let user_token = doc.data().token;
                                functions.logger.log('token: ', user_token);
                                tokens.push(user_token);
                            }
                        }).catch(err => {
                            functions.logger.error(err);
                        })
                    });
                });
        }).then(() => {

        }).catch(err => {
            functions.logger.error('Transaction error: ', err);
        })*/
    });