# P2Photo

## Introduction

A typical photo-sharing mobile application requires full trust in the application provider servers for maintaining users' photos and managing group membership. The downside of this web server architecture, however, is a loss of privacy, in the sense that the application provider can keep track not only of which photos are shared by the users, but also how they are accessed, by whom, and when. The goal of this project is to build P2Photo, a mobile application that allows users to share photos with their friends in a privacy-preserving way. This will be achieved by ensuring that the application provider will be involved only in maintaining group membership, i.e., allowing users to create new albums, finding new users, and adding or removing users from albums. In contrast, all the operations involving publishing photos in albums and reading photos from albums must be performed without the provider's awareness. Concretely, this means that the photos themselves must be stored and shared between users without the mediation of the provider. To this end, the project must support two architectures: cloud-backed (until the checkpoint) and wireless P2P (after the checkpoint).

## Specification

### Baseline Functionality

P2Photo is a mobile application that allows users to store photos on mobile devices without using centralized storage. Users can store photos, create albums, share albums with other users, provide photo storage for other users and use other users’ devices for photo storage. The basic architecture of P2Photo relies on a central server and a client mobile application.

#### Mobile Application Functionality

The P2Photo client is a mobile Android application that users install and run on their devices and allow users to perform the following functions:

**F1**. Sign up.
**F2**. Log in/out.
**F3**. Create albums.
**F4**. Find users.
**F5**. Add photos to albums.
**F6**. Add users to albums.
**F7**. List user’s albums.
**F8**. View album.

The sign up operation (F1) allows users to create a new user account in the system; the user must enter a username and password. The client then contacts the P2Photo server, which must ensure that the new username is unique and adds the user to the user database. If the operation is successful, the user can then log in and start a new session on the client device. 

To perform useful functions on P2Photo, the user must log into the system (F2) with his account credentials (username and password), which must be validated by the server. If they are valid, the server must generate a new session id, keep an internal record associating that session id to the username and return the new session id to the client. However, if the credentials are invalid, the server must return an error. Only users with valid session ids will be allowed to perform additional functions (F3-F8). The log out function (F2) ends the current session. 

Once a session is active, users are able to manage photos and albums. Users can create an album (F4). This operation creates an album catalog file in the P2Photo server containing the URL of an album-slice catalog of the creating user. The album-slice is the part of an album that contains the photos contributed by a user to an album. An album-slice is stored in its owner’s cloud storage. It includes all the photos that user had contributed to an album and a text file (the catalog) grouping all the URLs pointing to those photos. For example, if Alice creates an album “Fun”, automatically there is a album-slice in Alice’s cloud storage (e.g. Dropbox account) for that album “Fun” and a URL for that album-slice catalog. The album-slice URL points to an album-slice catalog which is a file in Alice’s cloud storage with a list of all the URLs of all the photos in Alice’s “Fun” album-slice.

P2Photo allows logged in users to find other users to create albums (F4). With the usernames returned by the server, a user can add those other users to the album membership (F6). Once a user is invited to an album, the application adds an album-slice to the album. If Alice invites Bob to join the “Fun” album, the catalog for that album (in the P2Photo server) will now include URLs for Alice’s album-slice catalog (in Alice’s cloud storage) and Bob’s album-slice catalog (in Bob’s cloud storage).