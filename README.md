# D-Fold

D-Fold is AWS Lambda powered distributed fold for Clojure.

## Features

* Same interface than `clojure.core.reducers/fold`
* Runs the code in parellal using AWS Lambda technology
* Runs CPU/IO bound tasks a lot faster

See more from my [presentation](https://www.youtube.com/watch?v=_9jdb5TOShk) at ClojuTre 2016 conference.

## Usage

Setup your project first (see example project).
After that you can use distributed fold in a following way:

```clojure
(require '[d-fold.core :refer [d-fold]])

(dfold + ((map inc) +) (range 10000) {:reduce-nodes 2 :aws-region "eu-west-1"})
```

If you make changes to your code update latest code to AWS Lambda environment:

  $ lein lambda update example

### Try the example project

Check you have your AWS credential setup.
Clone this repo and go to `examples` folder. Run:

  $ lein lambda install example

This will install AWS Lambda function named `d-fold` to region eu-west-1
with IAM role and policies configured.

Run example by starting repl:

  $ lein repl

Then run distributed sum function:

```clojure
(require '[example.core :refer [d-sum]])

(d-sum (range 10000))
```

This will use two parallel Lambda functions to calculate the result.
Note! For the first time it may take some time to run the code because
Lambda functions are cold. Further requests should be faster.

## Current limitations

* Maximum runtime of d-fold is 5 minutes
* No error handling (if your code crashes in Lambda you have to check error from Cloudwatch logs)
