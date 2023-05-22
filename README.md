# Lightest Ostis Pancake

## Installation

Clone repository:

```sh
git clone https://github.com/semantic-pie/lightest-ostis-pancake
```

To install the necessary components (sc-web, sc-machine, kb), run the following command:

```bash
./pancake.sh install
```

This will clone (or pull updates) the necessary components (sc-web, sc-machine) and clone all specified knowledge bases.

<br/>

## Adding a Local Knowledge Base

To add a knowledge base from a local directory, run the following command:

```bash
./pancake.sh add repo_name
```

Replace repo_name with the name of your local knowledge base directory. (in root dir)

<br/>

## Adding a Remote Knowledge Base

To add a knowledge base from a remote git repository, run the following command:

```bash
./pancake.sh add -u github_username/repo_name
```

Replace github_username/repo_name with the GitHub username and repository name of the knowledge base.

You can use the following:

```bash
./pancake.sh add -u <repo_url>:<repo_name> 
```

Replace <repo_url> with the URL of the git repository and <repo_name> with the desired name for the repository. If <repo_name> is not provided, it will default to the repository's base name.

<br/>

## Running OSTIS

To run ostis, use following command:

```bash
./pancake.sh run
```

Run in the background:

```bash
./pancake.sh run -d
```

Stop:

```bash
./pancake.sh stop
```

You can also:

```bash
./pancake.sh restart
```

<br/>

## Unplug Knowledge Base

To unplug a knowledge base, use following command:

```bash
./pancake.sh unplug knowledge_base_name
```

Replace knowledge_base_name with the name of the knowledge base you want to unplug.

<br/>

## Cleaning Knowledge Bases

To remove all installed knowledge base folders, execute the following command:

```bash
./pancake.sh clean
```

(it doesn't remove git repos urls from config)

<br/>

## Displaying Knowledge Bases

To display information about the knowledge bases in use, run the following command:

```bash
./pancake.sh info
```

This will show the list of local knowledge bases and synchronized git repositories.

<br/>

## Help

To display the usage information and available options, run the following command:

```bash
./pancake.sh --help
```
