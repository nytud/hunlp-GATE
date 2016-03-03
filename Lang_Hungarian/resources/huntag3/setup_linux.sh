#!/bin/sh

# The following installs all dependencies of Huntag3 on Ubuntu and Debian Linux systems.
# You will need superuser (sudo) privileges.

sudo apt-get install build-essential python3-dev python3-setuptools \
                     python3-numpy python3-scipy \
                     libatlas-dev libatlas3gf-base \
                     python3-pip
sudo pip3 install pyyaml scikit-learn
