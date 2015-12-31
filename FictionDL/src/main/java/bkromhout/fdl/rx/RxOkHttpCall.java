package bkromhout.fdl.rx;

import bkromhout.fdl.ex.RequestException;
import bkromhout.fdl.ex.ResponseException;
import bkromhout.fdl.util.C;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Transforms OkHttp Requests to Responses asynchronously.
 */
public class RxOkHttpCall implements Observable.Transformer<Request, Response> {

    @Override
    public Observable<Response> call(Observable<Request> requests) {
        return requests.flatMap(request -> Observable.create(new ExecuteRequest(request)));
    }

    /**
     * Allows for easy creation of Observables which return OkHttp Responses.
     */
    private static final class ExecuteRequest implements Observable.OnSubscribe<Response> {
        private final Request request;
        private final Executor cancellationExecutor;

        /**
         * Create a new {@link ExecuteRequest} which uses the Executor from the OkHttpClient dispatcher.
         * @param request Request to enqueue.
         */
        private ExecuteRequest(Request request) {
            this(request, C.getHttpClient().getDispatcher().getExecutorService());
        }

        /**
         * Create a new {@link ExecuteRequest} with a specific Executor.
         * @param request              Request to enqueue.
         * @param cancellationExecutor Executor of the OkHttpClient.
         */
        private ExecuteRequest(Request request, Executor cancellationExecutor) {
            this.request = request;
            this.cancellationExecutor = cancellationExecutor;
        }

        @Override
        public void call(final Subscriber<? super Response> sub) {
            // Create OkHttp Call.
            final Call call = C.getHttpClient().newCall(request);
            // Make sure that the request is cancelled when unsubscribing.
            sub.add(Subscriptions.create(() -> cancellationExecutor.execute(call::cancel)));

            // Enqueue the call.
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    if (sub.isUnsubscribed()) return;
                    sub.onError(new RequestException(request, e));
                }

                @Override
                public void onResponse(Response resp) throws IOException {
                    if (sub.isUnsubscribed()) return;
                    // Make sure the response is actually valid.
                    if (!resp.isSuccessful()) {
                        resp.body().close(); // Make sure the response body is closed so that it doesn't leak.
                        sub.onError(new ResponseException(
                                String.format(C.HTML_UNEXP_RESP, resp.request().urlString()), resp));
                    }
                    // If we were successful, notify the subscriber and then indicate we're complete.
                    sub.onNext(resp);
                    sub.onCompleted();
                }
            });
        }
    }
}
