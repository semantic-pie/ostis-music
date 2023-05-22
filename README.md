# Ostis Music

This repo is summary of [lightest-ostis-pancake](https://github.com/semantic-pie/lightest-ostis-pancake) and [music.ostis.kb](https://github.com/semantic-pie/music.ostis.kb)

## Installation

Clone repository:

```sh
git clone https://github.com/semantic-pie/lightest-ostis-pancake
```

To install the necessary components (sc-web, sc-machine, music.ostis.kb), run the following command:

```bash
./pancake.sh install
```

This will clone (or pull updates) the necessary components (sc-web, sc-machine) and clone all specified knowledge bases.

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

## Help

To display the usage information and available options, run the following command:

```bash
./pancake.sh --help
```
