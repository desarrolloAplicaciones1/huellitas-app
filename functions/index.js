const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notifyNewAlert = onDocumentCreated(
  { document: "alerts/{alertId}", region: "us-central1" },
  async (event) => {
    const alert = event.data?.data();
    if (!alert) return;

    const ownerId = alert.ownerId;
    const petName = alert.petName ?? "Mascota";
    const type = alert.type === "LOST" ? "perdida" : "encontrada";
    const address = alert.address ?? "zona desconocida";

    // Fetch all users except the creator
    const usersSnap = await getFirestore().collection("users").get();

    const tokens = [];
    usersSnap.forEach((doc) => {
      if (doc.id !== ownerId) {
        const token = doc.data().fcmToken;
        if (token) tokens.push(token);
      }
    });

    if (tokens.length === 0) {
      console.log("No hay tokens de FCM registrados para notificar.");
      return;
    }

    const message = {
      notification: {
        title: `Mascota ${type} cerca tuyo`,
        body: `${petName} — ${address}`,
      },
      data: {
        alertId: event.params.alertId,
        type: "NEW_ALERT",
      },
      tokens,
    };

    const response = await getMessaging().sendEachForMulticast(message);
    console.log(`Enviadas: ${response.successCount}, Fallidas: ${response.failureCount}`);

    // Remove invalid tokens from Firestore
    const invalidTokens = [];
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        const code = resp.error?.code;
        if (
          code === "messaging/invalid-registration-token" ||
          code === "messaging/registration-token-not-registered"
        ) {
          invalidTokens.push(tokens[idx]);
        }
      }
    });

    if (invalidTokens.length > 0) {
      const batch = getFirestore().batch();
      usersSnap.forEach((doc) => {
        if (invalidTokens.includes(doc.data().fcmToken)) {
          batch.update(doc.ref, { fcmToken: null });
        }
      });
      await batch.commit();
      console.log(`Tokens inválidos eliminados: ${invalidTokens.length}`);
    }
  }
);
