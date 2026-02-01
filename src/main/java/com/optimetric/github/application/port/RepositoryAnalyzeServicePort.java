package com.optimetric.github.application.port;

import com.optimetric.github.application.dto.AnalyzeResult;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Repo için hazırlık analizi ve eksik bileşenler için sync job'larını kuyruğa alma portu.
 */
public interface RepositoryAnalyzeServicePort {

    /**
     * Repo hazırlığını kontrol eder; eksikse uygun sync job'larını kuyruğa alır.
     *
     * @param login   Kullanıcı login (kimlik doğrulama)
     * @param fullName Repo tam adı (owner/repo)
     * @return AnalyzeResult (SYNC_STARTED, ALREADY_READY veya SYNC_ALREADY_RUNNING)
     */
    AnalyzeResult analyze(String login, String fullName) throws RepositoryNotFoundException, UserNotFoundException;
}
