export function typedAction<T extends string>(type: T): { type: T };

// tslint:disable-next-line: no-any
export function typedAction<T extends string, P extends any>(
  type: T,
  payload: P,
): { type: T; payload: P };

// tslint:disable-next-line: no-any
export function typedAction(type: string, payload?: any) {
  return { type, payload };
}
