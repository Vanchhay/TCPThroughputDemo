package app;

interface Publisher<T> {

    void register(Observer<T> subscriber);

    void unregister(Observer<T> subscriber);

}