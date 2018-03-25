package com.example.internetmanager.internet_connection_service;

import io.reactivex.Observable;

public interface InternetConnectionService {
    Observable<Boolean> hasConnection();

    void destroy();
}
