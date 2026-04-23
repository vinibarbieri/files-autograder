/**
 * Pure domain model for the autograder.
 *
 * <p>Allowed dependencies: JDK only. No Spring, no {@code docker-java}, no
 * filesystem or network access may appear in this package. Ports (interfaces
 * that outer layers implement) live here so the dependency rule in ADR-0008
 * is observed: arrows point inward.
 */
package com.autograder.domain;
