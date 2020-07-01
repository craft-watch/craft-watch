export const toSafePathPart = (text: string): string => text.toLowerCase().replace(/[^0-9a-z]/g, "-");
