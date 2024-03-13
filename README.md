# OSTIS music

## How to install

Open the terminal (command prompt) on your computer. Clone the repository containing our `control` script by executing the following command:

```bash
git clone https://github.com/semantic-pie/ostis-music
```

Navigate to the directory of our application:

```bash
cd ostis-music
```

## Launching app

> [!IMPORTANT]  
> A [docker](https://www.docker.com/) is required to successfully launch the application.

To run app, use following command:

```bash
docker compose up
```

Run in the background:

```bash
docker compose up -d
```

Stop:

```bash
docker compose down
```

## Track uploading

After the command `docker compose up` URLs will be accepted:

- `http://localhost:3000` - test-interface
- `http://localhost:8000` - sc-web

To add tracks to the knowledge base, go to the url `http://localhost:8000` (sc-web) and in the search field find a node named `file loader`. A track loader form will appear, select the tracks in mp3 format `Choose Files` and click `Upload`. After uploading, the tracks will be available in the test-interface (`http://localhost:3000`).

## "Usage"

Basically, all controls should be intuitive, but here are the basic interface functionality available now:

### Authorization (Optinal)

Authorization allows you to like tracks and generate a personalized playlist. But you can view and listen to music without authorization.
Authorization is as simple as possible, in the `test-interface` (<http://localhost:3000> ):
`SignUp` -> enter username, password + select your preferred genres (the list of genres is formed based on the genres of uploaded tracks) -> tap `SignUp`. If `Hi, {username}` appears, then everything is ok.

### Listening to tracks

Click and listen...

### Like tracks

Click like button...

### Generating a playlist based on preferences

Click `Generate playlist` button

### Open genreated playlist

Click `Open playlist` button

### Open liked tracks playlist

Click `Liked` button
