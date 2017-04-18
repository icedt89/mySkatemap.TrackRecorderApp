export class Exception {
    public constructor(public message: string) {
    }

    public toString(): string {
        return this.message;
    }
}