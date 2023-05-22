#!/bin/bash

WORKDIR=$(pwd)
KB_PATHS='repo.path'
touch $KB_PATHS # create if not exist
GIT_KB_PATHS='git.repo.path'
export GIT_TERMINAL_PROMPT=0

function usage() {
    cat <<USAGE

Usage:
    $0 [command] [options]

Commands:
    install      Install necessary components (sc-web, sc-machine) and clone knowledge bases
    clean        Remove all knowledge base folders
    add          Add a knowledge base from a local directory or a remote git repository
    run          Run ostis
    unplug       Remove a knowledge base from repo.path without deleting the directory
    info         Display information about the knowledge bases in use
    help         Show usage information

Description:
    pancake - script that allows you to install and manage knowledge bases.
    It can install the required components, clean up existing knowledge bases,
    and add new knowledge bases from git repositories.

USAGE
    exit 1
}

function run_usage() {
    cat <<USAGE

Usage:
    $0 run [-d]

Options:
    d      Detached run. Run docker containers in detached mod.

USAGE
    exit 1
}

function add_usage() {
    cat <<USAGE

Usage:
    $0 add [-u]

Options:
    u      Install from remote repository

USAGE
    exit 1
}


# ==============================================
# COMPONENTS PREPARE (SC-machine SC-web ProblemSover)

# clone / pull any component
function prepare_component() {
    REPO=$1
    NAME=$2
    echo -e "\033[1m[$NAME]\033[0m":
    if [ -e "$NAME" ]; then
        cd $NAME   
        git pull
    else
        git clone "$REPO" "$NAME"
    fi
    cd $WORKDIR
}

# clone / pull problem solver
# specific, cause its installation requires 
# injection to sc-machine CMakeList.txt
function prepare_problem_solver() {
    echo -e "\033[1m[problem-solver]\033[0m":
    if [ -e "sc-machine/problem-solver" ]; then
        cd sc-machine/problem-solver
        git pull
    else 
        cd sc-machine
        git clone https://github.com/semantic-pie/problem-solver
        echo 'add_subdirectory(${SC_MACHINE_ROOT}/problem-solver)' >> CMakeLists.txt
    fi
    cd $WORKDIR
}


# ==============================================
# UTILITIES 

# check if repo ever exist and clone
function clone_git_repo() {
    REPO=$1
    NAME=$2
    if git ls-remote --exit-code $REPO > /dev/null 2>&1; then
        # if repo exist - clone
        git clone --depth=1 "$REPO" "$NAME"
    else
        echo "Repo: [$REPO] not exist."
        exit 1
    fi
}

# add to repo.path if it's not there yet
function add_to_KB_PATHS() {
    if ! grep -Fxq "$1" "$KB_PATHS"; then
        echo "$1" >> $KB_PATHS
        echo "ADDED SUCCESSFULLY [$1]"
    else 
        echo "ALREDY EXIST [$1]"
        exit 1
    fi
}

# add to git.repo.path if it's not there yet
function add_to_GIT_KB_PATHS() {
    REPO=$1
    NAME=$2
    if git ls-remote --exit-code https://github.com/$REPO > /dev/null 2>&1; then
        if ! grep -q -E "$REPO|$NAME" "$GIT_KB_PATHS"; then
            echo "$REPO $NAME" >> $GIT_KB_PATHS
           
        else 
            echo "Repository [$REPO=$NAME] alredy exists"
        fi
    else
        echo "Repo: [https://github.com/$REPO] not exist."
        exit 1
    fi
}

# remove from repo.path
function remove_from_KB_PATHS() {
    TEMP="path.temp"
    sed "/$1/d" "$KB_PATHS" >> $TEMP
    mv $TEMP "$KB_PATHS"
}

# remove from git.repo.path
function remove_from_GIT_KB_PATHS() {
    TEMP="path.temp"
    sed "/$1/d" "$GIT_KB_PATHS" >> $TEMP
    mv $TEMP "$GIT_KB_PATHS"
}

# add to repo.path
function add_local_kb() {
    for kb_name in "$@"
    do
        if [[ -e $kb_name ]];then
            add_to_KB_PATHS $kb_name 
        else 
            echo "Directory: [$kb_name] not exist. (move your kb folder to the root directory)"
        fi
    done
}

# add to git.repo.path
function add_remote_kb() {
    for arg in "$@"
    do
        IFS=":" read -r repo_url repo_name <<< "$arg"
    
        if [[ ! $repo_url ]]; then
                exit
        fi
        
        if [[ ! $repo_name ]]; then
                repo_name=$(basename "$repo_url")
        fi
        add_to_GIT_KB_PATHS $repo_url $repo_name

        # install remote kb
        prepare_kb "https://github.com/$repo_url" "$repo_name"
    done 
}

# remove from repo.path
function safe_remove_local_kb() {
    for kb_name in "$@"
    do
        remove_from_KB_PATHS $kb_name
    done
}

# remove from git.repo.path
function safe_remove_remote_kb() {
    for kb_name in "$@"
    do
        remove_from_GIT_KB_PATHS $kb_name     
    done
}


# ==============================================
# KNOWlEDGE BASES PREPARE

# clone / pull any kb
function prepare_kb() {
    REPO=$1
    NAME=$2
    echo -e "\033[1m[$NAME]\033[0m":

    if [ -e "$NAME" ]; then
        cd "$NAME"
        git pull
    else
        # clone repo and (if success) add repo name to repo.path
        clone_git_repo "$REPO" "$NAME" && add_to_KB_PATHS "$NAME"
    fi
    cd $WORKDIR
}

# clones all kb-repositories from git.repo.path
function prepare_all_kb() {
    # If the file exists
    if [ ! -f "$GIT_KB_PATHS" ]; then
        echo "git.repo.path not found!"
        exit 1
    fi

    # Loop through the file line by line
    while read -r repo_url repo_name || [[ -n "$repo_url" ]]; do
        # simple comments
        if [[ $repo_url == \#* ]]; then
            continue
        fi
        # if empty line
        if [ -z "$repo_url" ]; then
            continue 
        elif [ -z "$repo_name" ]; then
            repo_name=$(basename "$repo_url")
        fi

        prepare_kb "https://github.com/$repo_url" "$repo_name"

    done < "$GIT_KB_PATHS"  
}


# remove all local kb-repositories
function remove_all_kb() {
    # If the file exists
    if [ ! -f "$KB_PATHS" ]; then
        echo "[repo.path] not found!"
        exit 1
    fi


    read -p "Are you sure you want to delete all knowledge bases? (y/n):" answer

    if [[ $answer == "y" || $answer == "Y" ]]; then
        while read -r repo_name; do
            if [[  $repo_name == \#* ]]; then
                continue
            fi
            rm -rf "$repo_name"
            remove_from_KB_PATHS "$repo_name"
        done < "$KB_PATHS"  
        echo "Removed."
    else
        echo "Canceled."
    fi
}

# display content of repo.path and git.repo.path 
function show_info() {
    echo -e "\033[1m[LOCAL STORAGES]\033[0m":
    while read -r repo_name; do
        if [[  $repo_name == \#* ]]; then
            continue
        fi
        if [[ -z "$repo_name" ]]; then
            continue 
        fi
        echo $repo_name
    done < "$KB_PATHS"  

    echo ""

    echo -e "\033[1m[SYNCHRONIZED GIT STORAGES]\033[0m":
    while read -r repo_url repo_name; do
        if [[ $repo_url == \#* ]]; then
            continue
        fi
        # if empty line
        if [ -z "$repo_url" ]; then
            continue 
        elif [ -z "$repo_name" ]; then
            repo_name=$(basename "$repo_url")
        fi
        echo -e "$repo_name\t[$repo_url]"
    done < "$GIT_KB_PATHS"  
}

# ==============================================
# COMMAND SWITCHER

case $1 in

# clone components
install)
    # clone vitally important components 
    prepare_component https://github.com/ostis-ai/sc-machine sc-machine
    prepare_component https://github.com/ostis-ai/sc-web sc-web
    prepare_problem_solver

    # clone knowledge bases
    prepare_all_kb
    ;;

# clean project (remove all kbs)
clean)
    remove_all_kb
    ;;

# add kb. Remote or local
add)
    shift 1;
    while getopts "uh" opt; do
        case $opt in
        u) IS_GIT_URL=1 ;;
        h) add_usage  ;;
        \?) echoerr "Invalid option -$OPTARG" && add_usage ;;
        esac
    done
    shift $((OPTIND - 1))

    if [[ $IS_GIT_URL ]]; then
        add_remote_kb $@
    else 
        add_local_kb $@
    fi
    ;;

# run ostis
run)
    shift 1;
    while getopts "dh" opt; do
        case $opt in
        d) DETACHED=1 ;;
        h) run_usage  ;;
        \?) echo "Invalid option -$OPTARG" && run_usage
            exit 1
             ;;
        esac
    done
    shift $((OPTIND - 1))

    if [[ $DETACHED ]]; then
        echo "STARTING..."
        docker compose up -d
    else 
        docker compose up
    fi
    ;;

# stop ostis
stop)
    shift 1;
    docker compose down 
    ;;

# restart ostis
restart)
    shift 1;
    docker compose down 
    prepare_all_kb
    docker compose up -d
    echo "[RESTARTED]"
    ;;

# unplug kb without complete removal 
unplug)
    shift 1; 
    for kb_name in "$@"
    do
        safe_remove_local_kb $@
        safe_remove_remote_kb $@
    done
    ;;

info)
    show_info
    ;;
# show help
--help)
    usage
    ;;
help)
    usage
    ;;
-h)
    usage
    ;;

# All invalid commands will invoke usage page
*)
    usage
    ;;
esac
