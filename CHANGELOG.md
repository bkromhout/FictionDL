# FictionDL Changelog

## Release 4.1.0
* **New:** Added the ability to put multiple local story folders in a single parent folder, which can then be pointed to by adding a line to the input file prefixed with `@fdl:ls_folder=`
* **New:** Added the ability to specify (or override) certain story details in the input file using "Detail Tags". See [this wiki page](../../wiki/Detail-Tags) for more details
* **Fixed:** Downloading stories from MuggleNew was broken due to the new site layout. This is now fixed! (Tested with the default Blue layout)

## Release 4.0.0
* **New:** Local stories feature (CLI ONLY!), see [this wiki page](../../wiki/Local-Stories) for more details
* **New:** Include a link to the story on the generated title page
* **Fixed:** Ampersand escaping/unescaping/*re-escaping*/😑 logic is now smarter and less likely to break chapter files in ePubs
* **Fixed:** Leaky code revolving around OkHttp Response objects
* **Fixed:** When starting the GUI from the CLI, other arguments won't be ignored anymore
* **Fixed:** Fail gracefully when we try to download stories from The Broom Cupboard but aren't logged in
* **Fixed:** The latest SIYE parsing bug, this time to do with not properly detecting and reporting invalid story links
* Consolidated various HTML cleaning functions, as they were becoming unwieldy in number
* Cleaned up Story exception throwing a bit to be less obtuse and more explicit
* Changes to `Chapter` creation, must be done using the newly created `ChapterSource` class, which enables cleaner, more generalized RxJava code, and therefore better concurrency-related improvements
* CLI logging with verbose mode on (`-v`) is now makes it more obvious what things are what (warnings, errors, etc.)
* Lots of refactoring for logging code to make it more helpful
* General cleanup and polishing

## Release 3.1.1
* First public release! See [the wiki](../../wiki) for more details.
* **Fixed** A few small bugs, since having the first public release contain bugs is just silly

## Release 3.1.0
* **New:** Added support for [The Broom Cupboard (NSFW)](http://thebroomcupboard.net), supports logging in
* **New:** The GUI's "Start" button now doubles as a "Stop" button
* **Improved:** The GUI progress bar is more granular now, it updates per chapter processed instead of per story
* **Fixed:** A potential bug with login support
* Actually created a `Site` class, finally, in order to have more elegant code, lots of refactoring involved


## Release 3.0.0
* **New:** Added support for [Ao3](http://archiveofourown.org), we download the ePubs that Ao3 already makes available
* **New:** Verbose mode, for both the CLI (`-v`) and the GUI (run the CLI with `-g`/`--gui` and `-v`)
* Integrated RxJava, mostly because it looked interesting, but also because it lets us do some very cool concurrency-related stuff
* Switched from using Jsoup's built in connections to [OkHttp](https://github.com/square/OkHttp) since it's more robust and performant, especially in combination with RxJava
* Large refactoring, most notably due to the conversion of some code to use RxJava, but also because we now have a new class called `EpubDL`, which along with `ParsingDL` now extends another new class simply called `Downloader`

## Release 2.0.1
* **Fixed:** Overzealous ampersand escaping in a few places
* **Fixed:** The obligatory SIYE parsing issue, though just a couple this time, to do with failing to get the characters and not parsing the whole summary
* **Fixed:** Bug where some MuggleNet stories had no details because they're in a different location in the HTML
* **Fixed:** A couple other invalid ePub edge cases with more HTML cleaning logic

## Release 2.0.0
* **New:** Added a GUI version! (Its log even has nice colors 😉)
* **New:** Added support for [MuggleNet Fan Fiction](http://fanfiction.mugglenet.com/), including login support
* **New:** Support for logging into sites where some stories cannot be accessed otherwise, see [this wiki page](../../wiki/Login-Support) for more details  
(Note: currently only MuggleNet, but any sites added in the future which have stories that cannot be accessed without logging in will also get login support)
* **New:** Another story detail, series, for sites which support it
* **New:** Populate the "identifier" and "publisher" ePub metadata fields with a story's URL and host site, respectively
* **Fixed:** Bug where, for FanFiction.net stories, we sometimes weren't correctly parsing one or either of the genres and characters details, since it's possible for either of both of them to be missing from the story details on the site
* **Fixed:** More parsing bugs with (unsurprisingly) SIYE, caused by *a few more things* that I didn't anticipate
* Added even more HTML cleaning logic

## Release 1.1.0
* **New:** Save *all* stories as ePubs instead of folders of HTML files
* Currently will populate the title, author, and description metadata fields for all ePub files.
* **New:** Ability to specify an output directory using the `-o` option (consequentially, the input file path now must be prefixed by `-i`)
* **New:** Grab even more story details (not all sites/stories support/have all of them):
* Characters
* Dates published/last updated
* Genres
* Status
* Story "type" (i.e., FFN's fandoms/crossovers, SIYE's categories)
* Warnings
* **Changed:** We now parse FanFiction.net stories ourselves instead of downloading ePubs through p0ody-files (because its ePubs didn't include as many details in their title pages as I wanted)
* **Fixed:** Lots of different bugs in SIYE parsing (SIYE's HTML structure is very poor)
* Made HTML sanitizing functions more robust to decrease the possibility of generating an invalid ePub
* Started using [Gradle](https://gradle.org) to build
* Large refactoring to make all downloader classes extend a new `ParsingDL` class and all story classes extend a new `Story` class for the purpose of making adding new sites easier in the future


## Release 1.0.0
* **Initial version**
* CLI only
* Supports scraping and saving HTML pages from [FictionHunt](http://fictionhunt.com) and [SIYE](http://siye.co.uk) to HTML files (along with a generated title page)
* Supports saving [FanFiction.net](https://www.fanfiction.net) stories as ePubs using [p0ody-files](http://p0ody-files.com/ff_to_ebook/)
* Scraped story details supported:
* Title
* Author
* Summary
* Rating
* Word Count
* Chapter Count
