// tslint:disable-next-line: no-any
export function getOrElse(data: {}, keys: string[], that: any): any {
  if (keys.length > 0) {
    let nextKey = keys[0];
    if (data !== null && nextKey in data && keys.length > 1) {
      return getOrElse(data[nextKey], keys.splice(1), that);
    } else if (data !== null && nextKey in data) {
      return data[nextKey];
    } else {
      return that;
    }
  } else {
    return that;
  }
}

export function convertNewlinesToBreaks(html: string) {
  return html.replace(/\n/g, '<br/>');
}

export function escapeHTML(html: string) {
  return new Option(html).innerHTML;
}
