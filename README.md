# P2Photo

**Course:** Mobile and Ubiquitous Computing  
**University:** Instituto Superior Técnico  
**Academic year:** 2018-19

### Team

- David Gonçalves
- João Pires
- Leonardo Troeira

### Assignment

See [Assignment.md](documentation/Assignment.md)

### Report

See [CMU-201819-ProjectReport.pdf](documentation/CMU-201819-ProjectReport.pdf)

### To do

- wifi direct
- tests
- availability

### Problems

Besides not finishing the implementation of WiFi direct, subsequent offline availability through caching, and not having a proper log with timestamps of the operations and jUnit tests, we (and the teacher) have identified some problems:

- The TCP socket is left open during the lifetime of the application. This consumes much battery of the device. Although opening sockets multiple times also consumes much battery, as opening a socket results in a spike of energy consumption that is higher than a socket that is already open. Therefore, the suggestion of improvement would be to open a socket for each request and leaving it open during a time interval (e.g. 30 seconds), if a new request is made during that interval, reuse the socket and refresh the time interval, if not, close the socket.
- The access token for the Google Drive account could be stored encrypted in the server, so that the user wouldn't need to log in if he were to change mobile or delete the app data.
- The back button shouldn't take us to activities that don't make sense (e.g. login activity).