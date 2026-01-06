import { useEffect, useCallback, useRef } from 'react';

interface UseAutoLogoutOptions {
    /**
     * Timeout duration in milliseconds
     * Default: 15 minutes (900000 ms)
     */
    timeout?: number;
    /**
     * Callback function triggered when timeout is reached
     */
    onLogout: () => void;
}

/**
 * Custom hook to track user inactivity and trigger auto-logout
 * 
 * @param options - Configuration options for auto-logout
 * @returns void
 * 
 * @example
 * ```tsx
 * useAutoLogout({
 *   timeout: 900000, // 15 minutes
 *   onLogout: () => setIsAuthenticated(false)
 * });
 * ```
 */
export const useAutoLogout = ({ timeout = 900000, onLogout }: UseAutoLogoutOptions): void => {
    const timeoutRef = useRef<number | null>(null);

    // Reset the inactivity timer
    const resetTimer = useCallback(() => {
        // Clear existing timer
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        // Set new timer
        timeoutRef.current = setTimeout(() => {
            onLogout();
        }, timeout);
    }, [timeout, onLogout]);

    // Handle user activity events
    const handleActivity = useCallback(() => {
        resetTimer();
    }, [resetTimer]);

    useEffect(() => {
        // List of events to track user activity
        const events: (keyof DocumentEventMap)[] = ['mousemove', 'keydown', 'click', 'scroll'];

        // Add event listeners
        events.forEach((event) => {
            document.addEventListener(event, handleActivity);
        });

        // Start the initial timer
        resetTimer();

        // Cleanup function
        return () => {
            // Remove event listeners
            events.forEach((event) => {
                document.removeEventListener(event, handleActivity);
            });

            // Clear timeout
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [handleActivity, resetTimer]);
};
