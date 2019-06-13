'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database.ref(`/notifications/{user_id}/{notification_id}`).onWrite((change, context) => {
    const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;
    
    console.log("The user Id is: ", user_id);
    /**Stops proceeding to the rest of the functionif the 
     * entry is deleted from database. If you want to work with 
     * what should happen when an entry is deleted, you can replace the 
     * line from "return console.log..."
     * */
    if (!change.after.val()) {
        return console.log('A Notification has been deleted from the database:', notification_id);
    }

    const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');

    return fromUser.then(fromUserResult => {
        const from_user_id = fromUserResult.val().from;

        console.log('You have new notification from :', notification_id);

        const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
        const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

        return Promise.all([userQuery, deviceToken]).then(result => {
            const userName = result[0].val();
            const token_id = result[1].val();

           

            const payload = {
                notification: {
                    title: "New Friend Request",
                    body: ` ${userName} has sent you a friend request`,
                    icon: "default",
                    click_action: "sj.android.com.lapit_TARGET_NOTIFICATION"
                },
                data: {
                    from_user_id: from_user_id
                }
            };
            return admin.messaging().sendToDevice(token_id, payload).then(response => {
                return console.log('this was the notification Feature');
            });

        });
                
            });
        
        
});

