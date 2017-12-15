# distributed-work-delegation

## master

Implemented communication with workers.

### Workflow overview

1. ```master``` contains controller ```WorkerController```, which will register workers with the master.
2. ```master``` accepts a GET request from the end-user containing the github URL to the repository
3. The URI will be used to get a list of files from the repository using a github API.
4. To demonstrate manager-worker communcation model, the master will create one request per registered worker. (More requests can be sent parallelly, but doing that beats the purpose of the assignment). It is assumed, for this purpose that the worker can process only a single request at a time.
5. Wraps the ```RestTemplate``` responses using ```CompletableFuture```, which allows for requests to be sent and received asynchronously. Files will be picked up from a ```BlockingQueue``` of files, for each request. In case of timeout or error from worker, the file URI will be put back into the pool of existing files. If response returns successfully, the file will be removed from original list of files.
6. Execution ends when a response has been received for all files.
7. Using ```CompletableFuture.allOf(futures)```, the responses are joined and average is calculated. This is done for every set of response from the workers.


## worker

Worker is a simple server which will register with the master on creation and un-register on exit. It accepts the URI of a gihub file, which will be used to retrieve the file contents using using github API. The complexity for that file will then be calculated by any simple library.