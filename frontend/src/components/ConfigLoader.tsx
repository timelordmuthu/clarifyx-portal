"use client";

import React, { useEffect, useState, ReactNode } from "react";

export default function ConfigLoader({ children }: { children: ReactNode }) {
    const [configLoaded, setConfigLoaded] = useState<boolean | null>(null);

    useEffect(() => {
        fetch("/config.json")
            .then((res) => {
                if (!res.ok) {
                    throw new Error("Failed to load config");
                }
                return res.json();
            })
            .then((data) => {
                // Storing the config globally so the rest of the app can use it
                if (typeof window !== "undefined") {
                    (window as any).__API_CONFIG__ = data;
                }
                setConfigLoaded(true);
            })
            .catch((error) => {
                console.error("Configuration Error:", error);
                setConfigLoaded(false);
            });
    }, []);

    if (configLoaded === null) {
        // Render a loading state while fetching the configuration
        return (
            <div className="min-h-screen flex items-center justify-center bg-zinc-950 text-white">
                <p className="animate-pulse">Loading Application Configuration...</p>
            </div>
        );
    }

    if (configLoaded === false) {
        // FLR-002: Graceful degradation with EXACT specified error text
        return (
            <div className="min-h-screen flex items-center justify-center bg-red-600 text-white p-6">
                <div className="text-center">
                    <h1 className="text-2xl font-bold">
                        Configuration Error - Failed to load API configuration. Please check if public/config.json exists.
                    </h1>
                </div>
            </div>
        );
    }

    // Configuration successfully loaded, render the rest of the application
    return <>{children}</>;
}
