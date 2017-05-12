export class DatabindablePromise<T> implements Promise<T> {
    private static promiseCache = {};

    public static databindable<T>(key: string, promise: Promise<T>): Promise<T> {
        let cachedPromise = this.promiseCache[key];
        if (!cachedPromise) {
            cachedPromise = new DatabindablePromise(promise);

            this.promiseCache[key] = promise;
        }

        return cachedPromise;
    }

    public static resolve<T>(key: string | null, value: T): Promise<T> {
        key = key || JSON.stringify(value);

        return DatabindablePromise.databindable(key, Promise.resolve(value));
    }

    private promise: Promise<T>;

    private constructor(promise: Promise<T>) {
        this.promise = promise;
    }

    public then<TResult1, TResult2>(onfulfilled?: (value: T) => TResult1 | PromiseLike<TResult1>, onrejected?: (reason: any) => TResult2 | PromiseLike<TResult2>): Promise<TResult1 | TResult2> {
      return this.promise.then(onfulfilled, onrejected);
    }

    public catch<TResult>(onrejected?: (reason: any) => TResult | PromiseLike<TResult>): Promise<T | TResult> {
        return this.promise.catch(onrejected);
    }

    [Symbol.toStringTag]: "Promise";
}