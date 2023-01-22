import axios from "axios";

const setAuthToken = token => {
  if (token) {
    // Apply to every req
    axios.defaults.headers.common["Authorization"] = token;
  } else {
    // Delete auth Header
    delete axios.defaults.headers.common["Authorization"];
  }
};

export default setAuthToken;
