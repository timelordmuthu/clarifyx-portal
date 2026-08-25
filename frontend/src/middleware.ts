import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Next.js Middleware for Route Protection.
 * 
 * Intercepts requests to protected dashboards and verifies the JWT token.
 * Validates RBAC to ensure users only access their designated routes.
 * 
 * Ref: BRD CHUNK-7 — Test Case SEC-002 (Role Access Restrictions)
 */
export function middleware(request: NextRequest) {
    // We assume the JWT is stored in an HttpOnly cookie named 'token' after login.
    // In some client-side architectures, it might be in localStorage, but Next.js middleware 
    // only has access to cookies. For this implementation, we check the cookie.
    const token = request.cookies.get('token')?.value;

    const path = request.nextUrl.pathname;

    // If there is no token, redirect to a login page (if accessing protected routes)
    if (!token) {
        return NextResponse.redirect(new URL('/login', request.url));
    }

    try {
        // Decode JWT Payload (Edge-compatible Base64 decoding)
        // Note: Signature verification is handled by the Java Backend. 
        // We trust the cookie here for routing, but API requests will still be fully verified.
        const payloadBase64 = token.split('.')[1];
        const decodedJson = atob(payloadBase64);
        const payload = JSON.parse(decodedJson);
        const role = payload.role;

        // RBAC Routing Rules (SEC-002)
        if (path.startsWith('/sales') && role !== 'SALES') {
            return redirectToRoleHome(role, request.url);
        }
        if (path.startsWith('/cfo') && role !== 'CFO') {
            return redirectToRoleHome(role, request.url);
        }
        if (path.startsWith('/finance') && role !== 'FINANCE') {
            return redirectToRoleHome(role, request.url);
        }
        if (path.startsWith('/admin') && role !== 'ADMIN') {
            return redirectToRoleHome(role, request.url);
        }

        // Allow request to proceed if roles match
        return NextResponse.next();
    } catch (e) {
        // If token decoding fails, force re-login
        console.error("JWT decoding failed in middleware:", e);
        return NextResponse.redirect(new URL('/login', request.url));
    }
}

/**
 * Redirects an unauthorized user back to their designated role dashboard.
 * Per instructions: Sales is redirected to /sales/request-creation.
 */
function redirectToRoleHome(role: string, baseUrl: string) {
    switch (role) {
        case 'SALES': 
            return NextResponse.redirect(new URL('/sales/request-creation', baseUrl));
        case 'CFO': 
            return NextResponse.redirect(new URL('/cfo/dashboard', baseUrl));
        case 'FINANCE': 
            return NextResponse.redirect(new URL('/finance/dashboard', baseUrl));
        case 'ADMIN': 
            return NextResponse.redirect(new URL('/admin/dashboard', baseUrl));
        default: 
            return NextResponse.redirect(new URL('/login', baseUrl));
    }
}

// Specify which paths the middleware should intercept
export const config = {
    matcher: ['/sales/:path*', '/cfo/:path*', '/finance/:path*', '/admin/:path*'],
};
