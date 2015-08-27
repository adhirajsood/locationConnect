package com.task.phone.joisterproject;

/**
 * Created by adhiraj on 26/8/15.
 */
public interface IAsyncCallback {
    public void onSuccessResponse(String successResponse);

    public void onErrorResponse(int errorCode , String errorResponse );
}
