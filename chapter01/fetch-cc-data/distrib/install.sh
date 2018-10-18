#!/bin/sh

rm -rf /usr/local/bin/fetch-cc-data-distrib
rm -f /usr/local/bin/fetch-cc-data
mkdir /usr/local/bin/fetch-cc-data-distrib
cp ./* /usr/local/bin/fetch-cc-data-distrib/
cp ./fetch-cc-data /usr/local/bin/
chmod +x /usr/local/bin/fetch-cc-data
echo "Installation successful."
