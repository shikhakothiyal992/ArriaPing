package com.arria.ping.model.responsehandlers

class Response<T> {
    val status: Status
    val message: String?
    val data: T?
    val code: Int
    val exception: Exception?
    val throwable: Throwable?

    constructor(
            status: Status, data: T?, message: String?, code: Int,
            e: Exception?
    ) {
        this.status = status
        this.data = data
        this.message = message
        this.code = code
        exception = e
        throwable = null
    }

    constructor(
            status: Status, data: T?, message: String?, code: Int,
            t: Throwable?
    ) {
        this.status = status
        this.data = data
        this.message = message
        this.code = code
        exception = null
        throwable = t
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null || javaClass != obj.javaClass) {
            return false
        }
        val resource = obj as Response<*>
        if (status !== resource.status) {
            return false
        }
        if (if (message != null) message != resource.message else resource.message != null) {
            return false
        }
        if (code != resource.code) {
            return false
        }
        return if (data != null) data == resource.data else resource.data == null
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", code=" + code +
                '}'
    }

    companion object {
        private const val NO_CODE = -1
        fun <T> success(data: T?): Response<T?> {
            return Response(Status.SUCCESS, data, null, NO_CODE, null)
        }

        fun <T> error(
            msg: String?, data: T?, code: Int,
            e: Exception?
        ): Response<T?> {
            return Response(Status.ERROR, data, msg, code, e)
        }

        fun <T> error(
            msg: String?, data: T?, code: Int,
            t: Throwable?
        ): Response<T?> {
            return Response(Status.ERROR, data, msg, code, t)
        }

        fun <T> loading(data: T?): Response<T?> {
            return Response(Status.LOADING, data, null, NO_CODE, null)
        }
    }
}