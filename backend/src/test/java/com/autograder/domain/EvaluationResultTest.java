package com.autograder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RED tests for roadmap cycle 2: {@link EvaluationResult} value object.
 *
 * <p>Drives factory methods and null-safety on a pure domain record.
 * No mocks, no Spring, no I/O.
 */
class EvaluationResultTest {

    @Test
    @DisplayName("pass() carries PASS verdict with no diff and no compile log")
    void pass_hasPassVerdictAndNoDiagnostics() {
        EvaluationResult result = EvaluationResult.pass();

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
        assertThat(result.diff()).isEmpty();
        assertThat(result.compileLog()).isEmpty();
    }

    @Test
    @DisplayName("fail(diff) carries FAIL verdict with the diff present")
    void fail_hasFailVerdictAndDiff() {
        EvaluationResult result = EvaluationResult.fail("- expected\n+ actual");

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).hasValue("- expected\n+ actual");
        assertThat(result.compileLog()).isEmpty();
    }

    @Test
    @DisplayName("compileError(log) carries COMPILE_ERROR verdict with the log present")
    void compileError_hasCompileErrorVerdictAndLog() {
        EvaluationResult result = EvaluationResult.compileError("main.c:1:1: error: expected ';'");

        assertThat(result.verdict()).isEqualTo(Verdict.COMPILE_ERROR);
        assertThat(result.diff()).isEmpty();
        assertThat(result.compileLog()).hasValue("main.c:1:1: error: expected ';'");
    }

    @Test
    @DisplayName("timeout() carries TIMEOUT verdict with no diagnostics")
    void timeout_hasTimeoutVerdictAndNoDiagnostics() {
        EvaluationResult result = EvaluationResult.timeout();

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
        assertThat(result.diff()).isEmpty();
        assertThat(result.compileLog()).isEmpty();
    }

    @Test
    @DisplayName("internalError(message) carries INTERNAL_ERROR verdict with the message in compileLog")
    void internalError_hasInternalErrorVerdictAndMessage() {
        EvaluationResult result = EvaluationResult.internalError("Docker daemon unreachable");

        assertThat(result.verdict()).isEqualTo(Verdict.INTERNAL_ERROR);
        assertThat(result.diff()).isEmpty();
        assertThat(result.compileLog()).hasValue("Docker daemon unreachable");
    }

    @Test
    @DisplayName("fail(diff) rejects null diff")
    void fail_rejectsNullDiff() {
        assertThatThrownBy(() -> EvaluationResult.fail(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("compileError(log) rejects null log")
    void compileError_rejectsNullLog() {
        assertThatThrownBy(() -> EvaluationResult.compileError(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("internalError(message) rejects null message")
    void internalError_rejectsNullMessage() {
        assertThatThrownBy(() -> EvaluationResult.internalError(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------- invalid state combinations (constructor-level invariants) ----------

    @Test
    @DisplayName("FAIL without diff is rejected")
    void constructor_rejects_failWithNoDiff() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.FAIL, Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("FAIL with compileLog is rejected")
    void constructor_rejects_failWithCompileLog() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.FAIL, Optional.of("diff"), Optional.of("log")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("COMPILE_ERROR without compileLog is rejected")
    void constructor_rejects_compileErrorWithNoLog() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.COMPILE_ERROR, Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("COMPILE_ERROR with diff is rejected")
    void constructor_rejects_compileErrorWithDiff() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.COMPILE_ERROR, Optional.of("diff"), Optional.of("log")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("INTERNAL_ERROR without compileLog is rejected")
    void constructor_rejects_internalErrorWithNoLog() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.INTERNAL_ERROR, Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("INTERNAL_ERROR with diff is rejected")
    void constructor_rejects_internalErrorWithDiff() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.INTERNAL_ERROR, Optional.of("diff"), Optional.of("msg")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PASS with diff is rejected")
    void constructor_rejects_passWithDiff() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.PASS, Optional.of("diff"), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PASS with compileLog is rejected")
    void constructor_rejects_passWithCompileLog() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.PASS, Optional.empty(), Optional.of("log")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TIMEOUT with diff is rejected")
    void constructor_rejects_timeoutWithDiff() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.TIMEOUT, Optional.of("diff"), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TIMEOUT with compileLog is rejected")
    void constructor_rejects_timeoutWithCompileLog() {
        assertThatThrownBy(() ->
                new EvaluationResult(Verdict.TIMEOUT, Optional.empty(), Optional.of("log")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
