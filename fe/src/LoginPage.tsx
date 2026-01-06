import React, { useState } from 'react';
import { Database, Mail, Lock } from 'lucide-react';

interface LoginPageProps {
    onLogin: () => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLogin }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage('');
        setIsLoading(true);

        // Simulate authentication delay
        await new Promise(resolve => setTimeout(resolve, 800));

        // Simple mock authentication
        if (username === 'admin' && password === 'password') {
            onLogin();
        } else {
            setErrorMessage('Invalid credentials. Please check your username and password.');
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-blue-100 to-slate-200 flex items-center justify-center p-4 sm:p-6 md:p-8 relative overflow-hidden">
            {/* Decorative Cloud/Light Elements */}
            <div className="absolute top-0 right-0 w-96 h-96 bg-white/30 rounded-full blur-3xl pointer-events-none"></div>
            <div className="absolute bottom-0 left-0 w-72 h-72 bg-blue-200/40 rounded-full blur-3xl pointer-events-none"></div>
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-slate-200/30 to-transparent rounded-full blur-3xl pointer-events-none"></div>

            <div className="w-full max-w-sm relative z-10">
                {/* Login Card */}
                <div className="bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl shadow-slate-900/10 border border-white/50 p-8 sm:p-10">
                    {/* Logo Container */}
                    <div className="flex justify-center mb-6">
                        <div className="bg-slate-100 p-3 rounded-xl ring-1 ring-slate-200/50">
                            <Database className="w-8 h-8 text-slate-700" />
                        </div>
                    </div>

                    {/* Header */}
                    <div className="text-center mb-8">
                        <h1 className="text-2xl sm:text-3xl font-bold text-slate-800 mb-3">
                            Sign in with email
                        </h1>
                        <p className="text-sm text-slate-500 leading-relaxed">
                            Access your data workspace securely
                        </p>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Email/Username Input */}
                        <div className="space-y-2">
                            <label
                                htmlFor="username"
                                className="block text-sm font-semibold text-slate-700"
                            >
                                Email or Username
                            </label>
                            <div className="relative group">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <Mail className="w-5 h-5 text-slate-400 group-focus-within:text-slate-700 transition-colors" />
                                </div>
                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="Enter your email"
                                    className="w-full pl-12 pr-4 py-3.5 bg-slate-100 border-2 border-transparent rounded-xl focus:outline-none focus:bg-white focus:border-slate-800 transition-all text-slate-900 placeholder:text-slate-400 font-medium shadow-sm"
                                    required
                                    disabled={isLoading}
                                />
                            </div>
                        </div>

                        {/* Password Input */}
                        <div className="space-y-2">
                            <label
                                htmlFor="password"
                                className="block text-sm font-semibold text-slate-700"
                            >
                                Password
                            </label>
                            <div className="relative group">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <Lock className="w-5 h-5 text-slate-400 group-focus-within:text-slate-700 transition-colors" />
                                </div>
                                <input
                                    id="password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Enter your password"
                                    className="w-full pl-12 pr-4 py-3.5 bg-slate-100 border-2 border-transparent rounded-xl focus:outline-none focus:bg-white focus:border-slate-800 transition-all text-slate-900 placeholder:text-slate-400 font-medium shadow-sm"
                                    required
                                    disabled={isLoading}
                                />
                            </div>
                        </div>

                        {/* Forgot Password Link */}
                        <div className="flex justify-end">
                            <button
                                type="button"
                                className="text-sm text-blue-600 hover:text-blue-700 hover:underline font-medium transition-colors"
                            >
                                Forgot password?
                            </button>
                        </div>

                        {/* Error Message */}
                        {errorMessage && (
                            <div className="bg-red-50 border-2 border-red-200 rounded-xl p-4">
                                <p className="text-sm text-red-700 font-medium text-center">
                                    {errorMessage}
                                </p>
                            </div>
                        )}

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold py-4 rounded-xl transition-all duration-300 shadow-lg shadow-slate-900/25 hover:shadow-xl hover:shadow-slate-900/30 hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:translate-y-0 flex items-center justify-center gap-2 mt-2"
                        >
                            {isLoading ? (
                                <>
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                                    <span>Signing in...</span>
                                </>
                            ) : (
                                'Sign in to your account'
                            )}
                        </button>
                    </form>

                    {/* Additional Info */}
                    <div className="mt-8 text-center">
                        <p className="text-xs text-slate-400">
                            Protected by enterprise-grade security
                        </p>
                    </div>
                </div>

                {/* Footer */}
                <div className="mt-6 text-center">
                    <p className="text-xs text-slate-500 font-medium">
                        Data Generator v1.3 · © 2024
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
