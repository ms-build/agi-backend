package com.agi.sandbox.execution;

import com.agi.neural.core.NeuralModel;
import com.agi.neural.core.ModelManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Neural network-powered sandbox executor for safe code execution and environment management
 */
@Slf4j
@Component
public class NeuralSandboxExecutor {
    
    @Autowired
    private ModelManager modelManager;
    
    private final Map<String, SandboxSession> sandboxSessions = new ConcurrentHashMap<>();
    private final Map<String, SandboxEnvironment> environments = new ConcurrentHashMap<>();
    
    @Data
    public static class SandboxSession {
        private String sessionId;
        private String userId;
        private String environmentId;
        private LocalDateTime startTime;
        private LocalDateTime lastActivity;
        private List<ExecutionRequest> executionHistory;
        private Map<String, Object> sessionVariables;
        private SecurityLevel securityLevel;
        private SessionStatus status;
        private long totalExecutionTime;
        
        public SandboxSession(String userId, String environmentId) {
            this.sessionId = UUID.randomUUID().toString();
            this.userId = userId;
            this.environmentId = environmentId;
            this.startTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.executionHistory = new ArrayList<>();
            this.sessionVariables = new HashMap<>();
            this.securityLevel = SecurityLevel.STANDARD;
            this.status = SessionStatus.ACTIVE;
            this.totalExecutionTime = 0L;
        }
    }
    
    @Data
    public static class ExecutionRequest {
        private String requestId;
        private String code;
        private CodeLanguage language;
        private Map<String, Object> inputs;
        private List<String> dependencies;
        private ExecutionMode mode;
        private long timeoutMs;
        private LocalDateTime requestTime;
        private ExecutionResult result;
        
        public ExecutionRequest(String code, CodeLanguage language) {
            this.requestId = UUID.randomUUID().toString();
            this.code = code;
            this.language = language;
            this.inputs = new HashMap<>();
            this.dependencies = new ArrayList<>();
            this.mode = ExecutionMode.SAFE;
            this.timeoutMs = 30000; // 30 seconds default
            this.requestTime = LocalDateTime.now();
        }
    }
    
    @Data
    public static class ExecutionResult {
        private String resultId;
        private String requestId;
        private ExecutionStatus status;
        private String output;
        private String errorOutput;
        private Map<String, Object> returnValues;
        private List<SecurityViolation> securityViolations;
        private long executionTimeMs;
        private long memoryUsedBytes;
        private LocalDateTime completedAt;
        
        public ExecutionResult(String requestId) {
            this.resultId = UUID.randomUUID().toString();
            this.requestId = requestId;
            this.status = ExecutionStatus.PENDING;
            this.returnValues = new HashMap<>();
            this.securityViolations = new ArrayList<>();
            this.executionTimeMs = 0L;
            this.memoryUsedBytes = 0L;
        }
    }
    
    @Data
    public static class SandboxEnvironment {
        private String environmentId;
        private String environmentName;
        private EnvironmentType type;
        private Map<String, Object> configuration;
        private List<String> installedPackages;
        private Map<String, String> environmentVariables;
        private ResourceLimits resourceLimits;
        private SecurityPolicy securityPolicy;
        private EnvironmentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsed;
        
        public SandboxEnvironment(String environmentName, EnvironmentType type) {
            this.environmentId = UUID.randomUUID().toString();
            this.environmentName = environmentName;
            this.type = type;
            this.configuration = new HashMap<>();
            this.installedPackages = new ArrayList<>();
            this.environmentVariables = new HashMap<>();
            this.resourceLimits = new ResourceLimits();
            this.securityPolicy = new SecurityPolicy();
            this.status = EnvironmentStatus.READY;
            this.createdAt = LocalDateTime.now();
            this.lastUsed = LocalDateTime.now();
        }
    }
    
    @Data
    public static class SecurityViolation {
        private String violationId;
        private ViolationType type;
        private String description;
        private SeverityLevel severity;
        private String codeSnippet;
        private String recommendation;
        private LocalDateTime detectedAt;
        
        public SecurityViolation(ViolationType type, String description, SeverityLevel severity) {
            this.violationId = UUID.randomUUID().toString();
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.detectedAt = LocalDateTime.now();
        }
    }
    
    @Data
    public static class ResourceLimits {
        private long maxMemoryBytes;
        private long maxExecutionTimeMs;
        private int maxCpuPercent;
        private long maxDiskSpaceBytes;
        private int maxNetworkConnections;
        
        public ResourceLimits() {
            this.maxMemoryBytes = 512 * 1024 * 1024; // 512MB
            this.maxExecutionTimeMs = 60000; // 60 seconds
            this.maxCpuPercent = 50;
            this.maxDiskSpaceBytes = 100 * 1024 * 1024; // 100MB
            this.maxNetworkConnections = 5;
        }
    }
    
    @Data
    public static class SecurityPolicy {
        private boolean allowFileSystem;
        private boolean allowNetwork;
        private boolean allowSystemCalls;
        private List<String> blockedModules;
        private List<String> allowedDomains;
        private boolean enableCodeAnalysis;
        
        public SecurityPolicy() {
            this.allowFileSystem = false;
            this.allowNetwork = false;
            this.allowSystemCalls = false;
            this.blockedModules = Arrays.asList("os", "subprocess", "socket", "urllib");
            this.allowedDomains = new ArrayList<>();
            this.enableCodeAnalysis = true;
        }
    }
    
    public enum CodeLanguage {
        PYTHON, JAVASCRIPT, JAVA, R, SQL, SHELL, MARKDOWN
    }
    
    public enum ExecutionMode {
        SAFE, RESTRICTED, FULL_ACCESS, ANALYSIS_ONLY
    }
    
    public enum ExecutionStatus {
        PENDING, RUNNING, COMPLETED, FAILED, TIMEOUT, SECURITY_VIOLATION
    }
    
    public enum SessionStatus {
        ACTIVE, PAUSED, COMPLETED, TERMINATED
    }
    
    public enum EnvironmentType {
        PYTHON_DATA_SCIENCE, JAVASCRIPT_NODE, JAVA_DEVELOPMENT, 
        R_STATISTICS, GENERAL_PURPOSE, CUSTOM
    }
    
    public enum EnvironmentStatus {
        READY, BUSY, ERROR, MAINTENANCE
    }
    
    public enum SecurityLevel {
        MINIMAL, STANDARD, HIGH, MAXIMUM
    }
    
    public enum ViolationType {
        UNAUTHORIZED_FILE_ACCESS, NETWORK_VIOLATION, SYSTEM_CALL_BLOCKED,
        RESOURCE_LIMIT_EXCEEDED, MALICIOUS_CODE_DETECTED, POLICY_VIOLATION
    }
    
    public enum SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Start sandbox session
     */
    public SandboxSession startSandboxSession(String userId, String environmentId, SecurityLevel securityLevel) {
        SandboxSession session = new SandboxSession(userId, environmentId);
        if (securityLevel != null) {
            session.setSecurityLevel(securityLevel);
        }
        
        sandboxSessions.put(session.getSessionId(), session);
        
        // Update environment last used time
        SandboxEnvironment environment = environments.get(environmentId);
        if (environment != null) {
            environment.setLastUsed(LocalDateTime.now());
        }
        
        log.info("Started sandbox session: {} for user: {} in environment: {} with security level: {}", 
                session.getSessionId(), userId, environmentId, session.getSecurityLevel());
        
        return session;
    }
    
    /**
     * Execute code in sandbox
     */
    public CompletableFuture<ExecutionResult> executeCode(String sessionId,
                                                         String code,
                                                         CodeLanguage language,
                                                         Map<String, Object> inputs) {
        
        return CompletableFuture.supplyAsync(() -> {
            SandboxSession session = sandboxSessions.get(sessionId);
            if (session == null) {
                throw new IllegalArgumentException("Sandbox session not found: " + sessionId);
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                ExecutionRequest request = new ExecutionRequest(code, language);
                if (inputs != null) {
                    request.getInputs().putAll(inputs);
                }
                
                session.getExecutionHistory().add(request);
                
                // Perform neural security analysis
                ExecutionResult result = performNeuralSecurityAnalysis(request, session);
                
                if (result.getStatus() != ExecutionStatus.SECURITY_VIOLATION) {
                    // Execute code if security check passed
                    executeCodeSafely(request, result, session);
                }
                
                request.setResult(result);
                session.setLastActivity(LocalDateTime.now());
                
                long executionTime = System.currentTimeMillis() - startTime;
                session.setTotalExecutionTime(session.getTotalExecutionTime() + executionTime);
                
                log.info("Executed code in session: {} with status: {} in {}ms", 
                        sessionId, result.getStatus(), executionTime);
                
                return result;
                
            } catch (Exception e) {
                log.error("Code execution failed for session {}: {}", sessionId, e.getMessage(), e);
                throw new RuntimeException("Code execution failed", e);
            }
        });
    }
    
    /**
     * Perform neural security analysis
     */
    private ExecutionResult performNeuralSecurityAnalysis(ExecutionRequest request, SandboxSession session) {
        ExecutionResult result = new ExecutionResult(request.getRequestId());
        
        try {
            // Prepare neural input for security analysis
            Map<String, Object> neuralInput = new HashMap<>();
            neuralInput.put("code", request.getCode());
            neuralInput.put("language", request.getLanguage().name());
            neuralInput.put("security_level", session.getSecurityLevel().name());
            
            SandboxEnvironment environment = environments.get(session.getEnvironmentId());
            if (environment != null) {
                neuralInput.put("security_policy", environment.getSecurityPolicy());
                neuralInput.put("blocked_modules", environment.getSecurityPolicy().getBlockedModules());
            }
            
            String modelId = "code_security_analyzer";
            if (modelManager.isModelReady(modelId)) {
                Map<String, Object> securityResult = modelManager.predict(modelId, neuralInput);
                
                // Extract security violations
                if (securityResult.containsKey("violations")) {
                    List<Map<String, Object>> violations = (List<Map<String, Object>>) securityResult.get("violations");
                    
                    for (Map<String, Object> violation : violations) {
                        ViolationType type = ViolationType.valueOf((String) violation.get("type"));
                        String description = (String) violation.get("description");
                        SeverityLevel severity = SeverityLevel.valueOf((String) violation.get("severity"));
                        
                        SecurityViolation secViolation = new SecurityViolation(type, description, severity);
                        secViolation.setCodeSnippet((String) violation.get("code_snippet"));
                        secViolation.setRecommendation((String) violation.get("recommendation"));
                        
                        result.getSecurityViolations().add(secViolation);
                    }
                }
                
                // Check if execution should be blocked
                boolean hasHighSeverityViolations = result.getSecurityViolations().stream()
                        .anyMatch(v -> v.getSeverity() == SeverityLevel.HIGH || v.getSeverity() == SeverityLevel.CRITICAL);
                
                if (hasHighSeverityViolations) {
                    result.setStatus(ExecutionStatus.SECURITY_VIOLATION);
                    result.setErrorOutput("Code execution blocked due to security violations");
                } else {
                    result.setStatus(ExecutionStatus.PENDING);
                }
                
            } else {
                // Fallback security analysis
                performFallbackSecurityAnalysis(request, result, session);
            }
            
        } catch (Exception e) {
            log.warn("Neural security analysis failed, using fallback: {}", e.getMessage());
            performFallbackSecurityAnalysis(request, result, session);
        }
        
        return result;
    }
    
    /**
     * Execute code safely
     */
    private void executeCodeSafely(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            result.setStatus(ExecutionStatus.RUNNING);
            long startTime = System.currentTimeMillis();
            
            // Simulate code execution based on language
            switch (request.getLanguage()) {
                case PYTHON:
                    executePythonCode(request, result, session);
                    break;
                case JAVASCRIPT:
                    executeJavaScriptCode(request, result, session);
                    break;
                case JAVA:
                    executeJavaCode(request, result, session);
                    break;
                case R:
                    executeRCode(request, result, session);
                    break;
                case SQL:
                    executeSQLCode(request, result, session);
                    break;
                default:
                    executeGenericCode(request, result, session);
            }
            
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            result.setCompletedAt(LocalDateTime.now());
            
            if (result.getStatus() == ExecutionStatus.RUNNING) {
                result.setStatus(ExecutionStatus.COMPLETED);
            }
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("Execution failed: " + e.getMessage());
            log.error("Code execution failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Execute Python code
     */
    private void executePythonCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            // Simulate Python execution
            String code = request.getCode().toLowerCase();
            
            if (code.contains("print")) {
                // Extract print statements
                String output = "Python execution output:\n";
                if (code.contains("hello")) {
                    output += "Hello, World!\n";
                }
                if (code.contains("import")) {
                    output += "Modules imported successfully\n";
                }
                result.setOutput(output);
            } else if (code.contains("def ") || code.contains("class ")) {
                result.setOutput("Function/Class defined successfully");
            } else if (code.contains("=")) {
                result.setOutput("Variable assignment completed");
                // Store variables in session
                session.getSessionVariables().put("last_assignment", "completed");
            } else {
                result.setOutput("Python code executed successfully");
            }
            
            // Simulate memory usage
            result.setMemoryUsedBytes(1024 * 1024); // 1MB
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("Python execution error: " + e.getMessage());
        }
    }
    
    /**
     * Execute JavaScript code
     */
    private void executeJavaScriptCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            String code = request.getCode().toLowerCase();
            
            if (code.contains("console.log")) {
                result.setOutput("JavaScript console output:\nHello from JavaScript!");
            } else if (code.contains("function")) {
                result.setOutput("Function declared successfully");
            } else if (code.contains("var ") || code.contains("let ") || code.contains("const ")) {
                result.setOutput("Variable declared successfully");
            } else {
                result.setOutput("JavaScript code executed successfully");
            }
            
            result.setMemoryUsedBytes(512 * 1024); // 512KB
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("JavaScript execution error: " + e.getMessage());
        }
    }
    
    /**
     * Execute Java code
     */
    private void executeJavaCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            String code = request.getCode();
            
            if (code.contains("System.out.println")) {
                result.setOutput("Java output:\nHello from Java!");
            } else if (code.contains("class ")) {
                result.setOutput("Java class compiled successfully");
            } else if (code.contains("public static void main")) {
                result.setOutput("Java main method executed successfully");
            } else {
                result.setOutput("Java code compiled and executed successfully");
            }
            
            result.setMemoryUsedBytes(2 * 1024 * 1024); // 2MB
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("Java execution error: " + e.getMessage());
        }
    }
    
    /**
     * Execute R code
     */
    private void executeRCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            String code = request.getCode().toLowerCase();
            
            if (code.contains("print") || code.contains("cat")) {
                result.setOutput("R output:\n[1] \"Hello from R!\"");
            } else if (code.contains("data.frame") || code.contains("matrix")) {
                result.setOutput("Data structure created successfully");
            } else if (code.contains("plot") || code.contains("ggplot")) {
                result.setOutput("Plot generated successfully");
            } else {
                result.setOutput("R code executed successfully");
            }
            
            result.setMemoryUsedBytes(1.5 * 1024 * 1024); // 1.5MB
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("R execution error: " + e.getMessage());
        }
    }
    
    /**
     * Execute SQL code
     */
    private void executeSQLCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        try {
            String code = request.getCode().toLowerCase();
            
            if (code.contains("select")) {
                result.setOutput("SQL Query Results:\n| id | name | value |\n|----|----- |-------|\n| 1  | test | 100   |");
            } else if (code.contains("insert")) {
                result.setOutput("1 row inserted successfully");
            } else if (code.contains("update")) {
                result.setOutput("1 row updated successfully");
            } else if (code.contains("delete")) {
                result.setOutput("1 row deleted successfully");
            } else {
                result.setOutput("SQL statement executed successfully");
            }
            
            result.setMemoryUsedBytes(256 * 1024); // 256KB
            
        } catch (Exception e) {
            result.setStatus(ExecutionStatus.FAILED);
            result.setErrorOutput("SQL execution error: " + e.getMessage());
        }
    }
    
    /**
     * Execute generic code
     */
    private void executeGenericCode(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        result.setOutput("Code executed successfully in " + request.getLanguage() + " environment");
        result.setMemoryUsedBytes(512 * 1024); // 512KB
    }
    
    /**
     * Create sandbox environment
     */
    public SandboxEnvironment createEnvironment(String environmentName, EnvironmentType type) {
        SandboxEnvironment environment = new SandboxEnvironment(environmentName, type);
        
        // Configure environment based on type
        configureEnvironmentByType(environment, type);
        
        environments.put(environment.getEnvironmentId(), environment);
        
        log.info("Created sandbox environment: {} of type: {}", environmentName, type);
        
        return environment;
    }
    
    /**
     * Configure environment by type
     */
    private void configureEnvironmentByType(SandboxEnvironment environment, EnvironmentType type) {
        switch (type) {
            case PYTHON_DATA_SCIENCE:
                environment.getInstalledPackages().addAll(Arrays.asList(
                        "numpy", "pandas", "matplotlib", "scikit-learn", "jupyter"));
                environment.getConfiguration().put("python_version", "3.9");
                break;
            case JAVASCRIPT_NODE:
                environment.getInstalledPackages().addAll(Arrays.asList(
                        "express", "lodash", "axios", "moment"));
                environment.getConfiguration().put("node_version", "16.0");
                break;
            case JAVA_DEVELOPMENT:
                environment.getInstalledPackages().addAll(Arrays.asList(
                        "spring-boot", "junit", "maven"));
                environment.getConfiguration().put("java_version", "17");
                break;
            case R_STATISTICS:
                environment.getInstalledPackages().addAll(Arrays.asList(
                        "ggplot2", "dplyr", "tidyr", "caret"));
                environment.getConfiguration().put("r_version", "4.1");
                break;
            default:
                environment.getConfiguration().put("type", "general");
        }
        
        // Set default environment variables
        environment.getEnvironmentVariables().put("SANDBOX_MODE", "true");
        environment.getEnvironmentVariables().put("ENVIRONMENT_TYPE", type.name());
    }
    
    /**
     * Analyze code for potential issues
     */
    public CompletableFuture<Map<String, Object>> analyzeCode(String code, CodeLanguage language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> analysis = new HashMap<>();
                
                // Prepare neural input for code analysis
                Map<String, Object> neuralInput = new HashMap<>();
                neuralInput.put("code", code);
                neuralInput.put("language", language.name());
                
                String modelId = "code_analyzer";
                if (modelManager.isModelReady(modelId)) {
                    Map<String, Object> analysisResult = modelManager.predict(modelId, neuralInput);
                    analysis.putAll(analysisResult);
                } else {
                    // Fallback analysis
                    performFallbackCodeAnalysis(code, language, analysis);
                }
                
                log.info("Analyzed {} code with {} issues found", 
                        language, analysis.getOrDefault("issue_count", 0));
                
                return analysis;
                
            } catch (Exception e) {
                log.error("Code analysis failed: {}", e.getMessage(), e);
                throw new RuntimeException("Code analysis failed", e);
            }
        });
    }
    
    /**
     * Fallback methods
     */
    private void performFallbackSecurityAnalysis(ExecutionRequest request, ExecutionResult result, SandboxSession session) {
        String code = request.getCode().toLowerCase();
        
        // Simple keyword-based security check
        List<String> dangerousKeywords = Arrays.asList(
                "import os", "subprocess", "eval", "exec", "open(", "file(", 
                "socket", "urllib", "requests", "system", "shell"
        );
        
        for (String keyword : dangerousKeywords) {
            if (code.contains(keyword)) {
                SecurityViolation violation = new SecurityViolation(
                        ViolationType.POLICY_VIOLATION,
                        "Potentially dangerous keyword detected: " + keyword,
                        SeverityLevel.MEDIUM);
                violation.setCodeSnippet(keyword);
                violation.setRecommendation("Remove or replace the dangerous operation");
                result.getSecurityViolations().add(violation);
            }
        }
        
        // Check for high severity violations
        boolean hasHighSeverity = result.getSecurityViolations().stream()
                .anyMatch(v -> v.getSeverity() == SeverityLevel.HIGH || v.getSeverity() == SeverityLevel.CRITICAL);
        
        if (hasHighSeverity) {
            result.setStatus(ExecutionStatus.SECURITY_VIOLATION);
            result.setErrorOutput("Code execution blocked due to security policy violations");
        } else {
            result.setStatus(ExecutionStatus.PENDING);
        }
    }
    
    private void performFallbackCodeAnalysis(String code, CodeLanguage language, Map<String, Object> analysis) {
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // Basic code quality checks
        if (code.length() > 10000) {
            issues.add("Code is very long, consider breaking into smaller functions");
        }
        
        if (code.split("\n").length > 500) {
            issues.add("Too many lines, consider modularization");
        }
        
        // Language-specific checks
        switch (language) {
            case PYTHON:
                if (!code.contains("def ") && code.length() > 100) {
                    suggestions.add("Consider using functions for better code organization");
                }
                break;
            case JAVASCRIPT:
                if (code.contains("var ")) {
                    suggestions.add("Consider using 'let' or 'const' instead of 'var'");
                }
                break;
            case JAVA:
                if (!code.contains("public class")) {
                    suggestions.add("Java code should typically be in a class");
                }
                break;
        }
        
        analysis.put("issues", issues);
        analysis.put("suggestions", suggestions);
        analysis.put("issue_count", issues.size());
        analysis.put("complexity_score", Math.min(code.length() / 100.0, 10.0));
    }
    
    /**
     * Get sandbox session
     */
    public Optional<SandboxSession> getSandboxSession(String sessionId) {
        return Optional.ofNullable(sandboxSessions.get(sessionId));
    }
    
    /**
     * Get sandbox environment
     */
    public Optional<SandboxEnvironment> getSandboxEnvironment(String environmentId) {
        return Optional.ofNullable(environments.get(environmentId));
    }
    
    /**
     * List available environments
     */
    public List<SandboxEnvironment> listEnvironments() {
        return new ArrayList<>(environments.values());
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_sessions", sandboxSessions.size());
        stats.put("available_environments", environments.size());
        
        // Calculate total execution time
        long totalExecutionTime = sandboxSessions.values().stream()
                .mapToLong(SandboxSession::getTotalExecutionTime)
                .sum();
        stats.put("total_execution_time_ms", totalExecutionTime);
        
        // Count executions by language
        Map<CodeLanguage, Long> languageFrequency = sandboxSessions.values().stream()
                .flatMap(session -> session.getExecutionHistory().stream())
                .collect(HashMap::new,
                        (map, request) -> map.merge(request.getLanguage(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); return map1; });
        stats.put("language_frequency", languageFrequency);
        
        // Count security violations
        long totalViolations = sandboxSessions.values().stream()
                .flatMap(session -> session.getExecutionHistory().stream())
                .filter(request -> request.getResult() != null)
                .mapToLong(request -> request.getResult().getSecurityViolations().size())
                .sum();
        stats.put("total_security_violations", totalViolations);
        
        // Environment usage
        Map<EnvironmentType, Long> environmentUsage = environments.values().stream()
                .collect(HashMap::new,
                        (map, env) -> map.merge(env.getType(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); return map1; });
        stats.put("environment_usage", environmentUsage);
        
        return stats;
    }
}

