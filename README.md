# Media Library

After digitizing most of my DVD collection, I wanted to have a way to browse these files in a nice way. Rather than use an open source program like [xbmc](http://www.raspbmc.com/) or [Plex](https://plex.tv/), I decided to write my own - mostly so I could get a bit more experience in writing clojure and clojurescript programs.

The system consists of two parts - one is a CLI program that scans a folder structure and then queries [The Movie Database API](http://docs.themoviedb.apiary.io/) to retrieve the metadata for each film and build up a library `data.edn` file. It also downloads the associated poster image for each file and resizes it to the correct size.

The other part is an embedded web-server that hosts static resources that parse the library file and display the collection - allowing the user to search, filter and launch content. When a request to launch content is made the server will shell out to a video player on the server and launch the video in fullscreen. As the content is not streamed, then it requires the computer running the webserver to have a head - which is fine given my living room setup :-).

I've published an example of the application on gh-pages using some fake data (This is fully functional barring the fact that obviously launching videos doesn't work as there is no serverside component running!)

This example app an be seen [here](http://optimisticpanda.co.uk/media-library/).

## Installation

Build the web-server by running: 

> lein uberjar

Create an account and create an api key from [The Movie Database API](https://www.themoviedb.org/login) and then populate the config file as follows:

```clojure
{
  :movie-db-api-key "${PERSONAL MOVIE DB API KEY}"
  :port             3000
  :library-loc      "<ROOT DIR FOR LIBRARY>"
  :launch-command   ["vlc" "--fullscreen"]
}
```

The `library-loc` variable should point to a directory that contains the following structure: 
```
Video/
├── Movies
│   ├── 1
│   ├── 2
│   ├── 3
│   ├── 4
│   ├── 5
│   ├── 6
│   └── 7
└── Tv
    ├── Adam and Joe
    │   └── Best Bits
    ├── Adventure Time
    │   ├── Series 1
    │   ├── Series 2
    │   ├── Series 3
    │   ├── Series 4
    │   └── Series 5
    ├── Alan Partridge
    │   ├── Extra
    │   ├── Series 1
    │   └── Series 2
...
```
Any files in the directory structure under `Video/Movies` will be picked up as a video file.
Individual episodes of a show need to be listed in: 

> Video/Tv/${show name}/${series name}/${file with episode name as filename} 

## Usage

The library-builder is currently only available via the repl and be launched by running the following main function:
	
> media-library.build > (-main)  

Once the `data.edn` file has been generated, run the web-server with the following command:

>    $ java -jar movie-details-grab-0.1.0-standalone.jar 

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
