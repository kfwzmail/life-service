package com.std.lifeService.security;

public class LoginUserContext {

    private static final ThreadLocal<LoginUser> USER_CONTEXT = new ThreadLocal<>();

    public static void set(LoginUser user) {
        USER_CONTEXT.set(user);
    }

    public static LoginUser get() {
        return USER_CONTEXT.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static void remove() {
        USER_CONTEXT.remove();
    }
}
