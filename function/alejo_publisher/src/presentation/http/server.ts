import "dotenv/config";
import cors from "cors";
import express, { type NextFunction, type Request, type Response } from "express";
import Pusher from "pusher";
import { PublishSaleVerificationUseCase } from "../../application/usecase/PublishSaleVerificationUseCase.js";
import type { PublishSaleVerificationCommand } from "../../domain/entity/PublishSaleVerificationCommand.js";
import { readEnv } from "../../infrastructure/config/env.js";
import { PusherSaleRealtimePublisher } from "../../infrastructure/realtime/PusherSaleRealtimePublisher.js";

const env = readEnv();
const app = express();

app.use(cors({
  origin: env.allowOrigin === "*"
    ? true
    : env.allowOrigin.split(",").map((item: string) => item.trim())
}));
app.use(express.json());

const pusher = new Pusher({
  appId: env.pusherAppId,
  key: env.pusherKey,
  secret: env.pusherSecret,
  cluster: env.pusherCluster,
  useTLS: true
});

const publishSaleVerificationUseCase = new PublishSaleVerificationUseCase(
  new PusherSaleRealtimePublisher(pusher)
);

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    service: "alejo_publisher"
  });
});

app.post("/sale-verification/publish", requireApiKey, async (req, res, next) => {
  try {
    const command = req.body as PublishSaleVerificationCommand;
    const result = await publishSaleVerificationUseCase.execute(command);
    res.json({
      ok: true,
      ...result
    });
  } catch (error) {
    next(error);
  }
});

app.use((error: unknown, _req: Request, res: Response, _next: NextFunction) => {
  const message = error instanceof Error ? error.message : "Unexpected error";
  const status = message.includes("obligatorio") || message.includes("decision debe") ? 400 : 500;
  res.status(status).json({
    ok: false,
    error: message
  });
});

app.listen(env.port, () => {
  console.log(`[alejo_publisher] listening on port ${env.port}`);
});

function requireApiKey(req: Request, res: Response, next: NextFunction): void {
  const authHeader = req.header("Authorization") ?? "";
  const token = authHeader.startsWith("Bearer ") ? authHeader.slice("Bearer ".length).trim() : "";

  if (!token || token !== env.apiKey) {
    res.status(401).json({
      ok: false,
      error: "Unauthorized"
    });
    return;
  }

  next();
}
