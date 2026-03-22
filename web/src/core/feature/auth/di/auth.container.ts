import {UserNetRepositoryImpl} from "../data/repository/user.net.repository";
import {infrastructureContainer} from "../../../infrastructure/di/infrastructure.container";
import {CreateAccountCaseUse} from "../domain/caseuse/CreateAccountCaseUse";
import {UpdateNameCaseUse} from "../domain/caseuse/UpdateNameCaseUse";
import {UpdatePasswordCaseUse} from "../domain/caseuse/UpdatePasswordCaseUse";
import {DeleteUserCaseUse} from "../domain/caseuse/DeleteUserCaseUse";
import {UpdateRoleCaseUse} from "../domain/caseuse/UpdateRoleCaseUse";
import {UpdatePhotoCaseUse} from "../domain/caseuse/UpdatePhotoCaseUse";
import {UpdatePhoneCaseUse} from "../domain/caseuse/UpdatePhoneCaseUse";
import {SessionNetManagerImpl} from "../data/repository/session.net.manager";
import {OpenSessionCaseUse} from "../domain/caseuse/OpenSessionCaseUse";
import {CloseSessionsCaseUSe} from "../domain/caseuse/CloseSessionsCaseUSe";
import {GetCurrentUserCaseUse} from "../domain/caseuse/GetCurrentUserCaseUse";
import {AdminNetManagerImpl} from "../data/repository/admin.repository";
import {GetAllUsersCaseUse} from "../domain/caseuse/GetAllUsersCaseUse";
import {LinkGoogleAccountCaseUse} from "../domain/caseuse/LinkGoogleAccountCaseUse";
import { CreateManagedUserCaseUse } from "../domain/caseuse/CreateManagedUserCaseUse";
import {
    UpdateManagedUserLabelsCaseUse,
    UpdateManagedUserPasswordCaseUse,
    UpdateManagedUserStatusCaseUse
} from "../domain/caseuse/UpdateManagedUserCaseUses";
import { GoogleAuthNetRepositoryImpl } from "../data/repository/google-auth.repository";
import { ExchangeGoogleCredentialCaseUse } from "../domain/caseuse/ExchangeGoogleCredentialCaseUse";
import { PasswordResetNetRepositoryImpl } from "../data/repository/password-reset.repository";
import { RequestPasswordResetCodeCaseUse } from "../domain/caseuse/RequestPasswordResetCodeCaseUse";
import { ConfirmPasswordResetCodeCaseUse } from "../domain/caseuse/ConfirmPasswordResetCodeCaseUse";

// Infraestructura Appwrite activa para el flujo MVP.
const accounts = infrastructureContainer.appwrite.account
const functions = infrastructureContainer.appwrite.functions

// Repositorios activos del MVP.
const authNetRepository = new UserNetRepositoryImpl(accounts)
const sessionNetManager = new SessionNetManagerImpl(accounts)
const adminNetRepository = new AdminNetManagerImpl(functions)

// Infraestructura futura:
// este repositorio HTTP permite volver a un flujo server-assisted para Google
// sin tocar la API pública del contenedor. Hoy el login/registro MVP no lo usa.
const googleAuthNetRepository = new GoogleAuthNetRepositoryImpl()

// Servicio activo para reset de contraseña externo.
const passwordResetNetRepository = new PasswordResetNetRepositoryImpl()

// Casos de uso del dominio.
const createAccountCaseUse = new CreateAccountCaseUse(authNetRepository)
const updateNameCaseUse = new UpdateNameCaseUse(authNetRepository)
const updatePasswordCaseUse = new UpdatePasswordCaseUse(authNetRepository)
const updatePhotoUrlCaseUse = new UpdatePhotoCaseUse(authNetRepository)
const updatePhoneCaseUse = new UpdatePhoneCaseUse(authNetRepository)
const updateRoleCaseUse = new UpdateRoleCaseUse(authNetRepository)
const deleteUserCaseUse = new DeleteUserCaseUse(authNetRepository)
const opeSessionCaseUse = new OpenSessionCaseUse(sessionNetManager)
const closeSessionCaseUSe = new CloseSessionsCaseUSe(sessionNetManager)
const getCurrentUserCaseUse = new GetCurrentUserCaseUse(authNetRepository)
const getAllUserCaseUse = new GetAllUsersCaseUse(adminNetRepository)
const linkGoogleAccountCaseUse = new LinkGoogleAccountCaseUse(authNetRepository, sessionNetManager)
const exchangeGoogleCredentialCaseUse = new ExchangeGoogleCredentialCaseUse(googleAuthNetRepository)
const requestPasswordResetCodeCaseUse = new RequestPasswordResetCodeCaseUse(passwordResetNetRepository)
const confirmPasswordResetCodeCaseUse = new ConfirmPasswordResetCodeCaseUse(passwordResetNetRepository)
const createManagedUserCaseUse = new CreateManagedUserCaseUse(adminNetRepository)
const updateManagedUserLabelsCaseUse = new UpdateManagedUserLabelsCaseUse(adminNetRepository)
const updateManagedUserStatusCaseUse = new UpdateManagedUserStatusCaseUse(adminNetRepository)
const updateManagedUserPasswordCaseUse = new UpdateManagedUserPasswordCaseUse(adminNetRepository)

export const authContainer = {
    repositories: {
        accounts: authNetRepository,
        sessions: sessionNetManager,
        adminNetRepository: adminNetRepository,
        googleAuthNetRepository,
        passwordResetNetRepository
    },
    useCases: {
        accounts: {
            createAccount: createAccountCaseUse.execute.bind(createAccountCaseUse),
            getCurrentUser: getCurrentUserCaseUse.execute.bind(getCurrentUserCaseUse),
            updateName: updateNameCaseUse.execute.bind(updateNameCaseUse),
            updatePassword: updatePasswordCaseUse.execute.bind(updatePasswordCaseUse),
            updatePhotoUrl: updatePhotoUrlCaseUse.execute.bind(updatePhotoUrlCaseUse),
            updatePhone: updatePhoneCaseUse.execute.bind(updatePhoneCaseUse),
            updateRole: updateRoleCaseUse.execute.bind(updateRoleCaseUse),
            deleteUser: deleteUserCaseUse.execute.bind(deleteUserCaseUse),
            getAllUserCaseUse: getAllUserCaseUse.execute.bind(getAllUserCaseUse),
            adminCreateUser: createManagedUserCaseUse.execute.bind(createManagedUserCaseUse),
            adminUpdateLabels: updateManagedUserLabelsCaseUse.execute.bind(updateManagedUserLabelsCaseUse),
            adminUpdateStatus: updateManagedUserStatusCaseUse.execute.bind(updateManagedUserStatusCaseUse),
            adminUpdatePassword: updateManagedUserPasswordCaseUse.execute.bind(updateManagedUserPasswordCaseUse),
            linkGoogleAccount: linkGoogleAccountCaseUse.execute.bind(linkGoogleAccountCaseUse),
            exchangeGoogleCredential: exchangeGoogleCredentialCaseUse.execute.bind(exchangeGoogleCredentialCaseUse),
            requestPasswordResetCode: requestPasswordResetCodeCaseUse.execute.bind(requestPasswordResetCodeCaseUse),
            confirmPasswordResetCode: confirmPasswordResetCodeCaseUse.execute.bind(confirmPasswordResetCodeCaseUse),
        },
        sessions: {
            openSession: opeSessionCaseUse,
            closeSession: closeSessionCaseUSe,
        }
    }
}
