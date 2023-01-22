import { combineReducers } from "redux";
import errorReducers from "./errorReducer";
import authReducer from "./authReducer";
import profileReducer from "./profileReducer";
import postReducer from "./postReducer";

export default combineReducers({
  auth: authReducer,
  errors: errorReducers,
  profile: profileReducer,
  post: postReducer
});
