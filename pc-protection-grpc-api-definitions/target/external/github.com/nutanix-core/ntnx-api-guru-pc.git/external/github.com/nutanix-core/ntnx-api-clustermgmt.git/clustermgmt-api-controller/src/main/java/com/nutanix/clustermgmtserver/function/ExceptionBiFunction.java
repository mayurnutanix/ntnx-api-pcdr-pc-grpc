/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.function;

/**
 * Represents a function that accepts two arguments and produces a result.
 * Function may throw a checked exception.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of the thrown checked exception
 */
@FunctionalInterface
public interface ExceptionBiFunction<T, U, R, E extends Exception> {
  R apply(T t, U u) throws E;
}
