export default interface AudioDetails {
  url: string;
  blob: Blob;
  duration: {
    h: number;
    m: number;
    s: number;
  };
}
