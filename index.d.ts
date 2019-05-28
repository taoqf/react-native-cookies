
/**
 * set a cookie (IOS ONLY)
 */
export function set(optons?: {
	name: string;
	value: string;
	domain: string;
	origin: string;
	path: string;
	version: '1',
	expiration: string;
}): Promise<void>;

/**
 * Set cookies from a response header
 */
export function setFromResponse(domain: string, value: string): Promise<void>;

/**
 * Get cookies from a response header
 */
export function getFromResponse(domain: string): Promise<{ [key: string]: string; }>;

/**
 * Get cookies as a request header string
 */
export function get(domain: string, useWebKit?: boolean): Promise<string>;

// list cookies (IOS ONLY)
export function getAll(useWebKit?: boolean): Promise<{ [domain: string]: string[]; }>;

// clear cookies
export function clearAll(useWebKit?: boolean): Promise<void>;

// clear a specific cookie by its name (IOS ONLY)
export function clearByName(name: string): Promise<void>;
