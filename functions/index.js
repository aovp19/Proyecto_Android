const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notificarMensaje = onDocumentCreated(
  {
    document: "chats/{chatId}/mensajes/{mensajeId}",
    region: "us-central1", // debe coincidir con la región de tu Firestore (Firestore > Data)
  },
  async (event) => {
    const mensaje = event.data?.data();
    if (!mensaje) return;

    // el chatId es "uidA_uidB": el receptor es el que no envió el mensaje
    const idReceptor = event.params.chatId
      .split("_")
      .find((uid) => uid !== mensaje.idEmisor);
    if (!idReceptor) return;

    const usuario = await getFirestore().collection("usuarios").doc(idReceptor).get();
    const tokens = usuario.get("fcmTokens") || [];
    if (tokens.length === 0) return;

    const respuesta = await getMessaging().sendEachForMulticast({
      tokens,
      data: {
        idEmisor: mensaje.idEmisor,
        nombreEmisor: mensaje.nombreEmisor || "",
        texto: mensaje.imagenUrl ? "📷 Imagen" : mensaje.texto || "",
      },
      android: { priority: "high" },
    });

    // borra los tokens que ya no sirven
    const invalidos = [];
    respuesta.responses.forEach((r, i) => {
      const codigo = r.error?.code;
      if (
        codigo === "messaging/registration-token-not-registered" ||
        codigo === "messaging/invalid-registration-token"
      ) {
        invalidos.push(tokens[i]);
      }
    });
    if (invalidos.length > 0) {
      await usuario.ref.update({ fcmTokens: FieldValue.arrayRemove(...invalidos) });
    }
  }
);