# D-Fold

D-Fold is AWS Lambda powered distributed fold for Clojure.

Background: See my presentation https://www.youtube.com/watch?v=_9jdb5TOShk

## Usage

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

