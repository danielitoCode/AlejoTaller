import {NavController} from "./NavController";

export function rememberNavController(startDestination: string) {
    return new NavController(startDestination);
}