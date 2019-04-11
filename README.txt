Project 4 Haiyu Wan 458812

• (10 points) User can login/logout, and login information is stored using Firebase

• (20 points) On login, there is a map of the current area which displays pins corresponding to photos

• (10 points) Logged in user’s posts are differentiated in a very clear way

(15 points) Clicking on a pin opens a new fragment/activity with the posters username, the date/location the photo was taken, and the photo itself

(20 points) User can click button to take a photo to post, which stores the photo and all relevant information

(10 points) Map updates immediately with the users new post

(15 points) Creative portion(s) - build your own feature:
1. Gesture: 
In the detail page activity,  the use can not only use the buttons to do operations but also use the gesture.
When user swipe to right,  user can back to the map page.
When user swipe to left, user can go to the comment page.
When user double tap, user can add new comment.

2. Add comments:
User can add their comment about the photo and all the other users can see it on a new activity ( commentActivity).  You can add comment by the float button at the conner of the detail activity or use the gesture on detail page. The comments will show the user who write the comment and body. All the information about the comment will be added into the firebase. The empty comment will be detected and show the alert toast. If you add the comment successfully, you will also see a toast.