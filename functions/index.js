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

        return admin.firestore().runTransaction(t => {
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
                        return  admin.messaging().sendToDevice(tokens, payload);
                    } else {
      functions.logger.log('Tokens array is empty');
        }

        }).catch(err => {
            functions.logger.error('Transaction error: ', err);
        })
    });
