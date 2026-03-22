export type GoogleAuthExchangeResult =
    | {
          kind: "session";
          userId: string;
          secret: string;
          email: string;
          sub: string;
          name?: string;
          picture?: string;
      }
    | {
          kind: "link_required";
          email: string;
          sub: string;
          name?: string;
          picture?: string;
      }
    | {
          kind: "register_required";
          email: string;
          sub: string;
          name?: string;
          picture?: string;
      };

export interface GoogleAuthNetRepository {
    exchangeCredential(params: { credential: string; allowCreate?: boolean }): Promise<GoogleAuthExchangeResult>;
}

