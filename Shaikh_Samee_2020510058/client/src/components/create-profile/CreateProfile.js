import React, { Component } from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import PropTypes from "prop-types";
import TextFiledGroup from "../common/TextFieldGroup";
import TextAreaFiledGroup from "../common/TextAreaFieldGroup";
import InputGroup from "../common/InputGroup";
import SelectListGroup from "../common/SelectListGroup";
import { createProfile } from "../../actions/profileActions";

class CreateProfile extends Component {
  constructor(props) {
    super(props);
    this.state = {
      displaySocialInput: false,
      handle: "",
      company: "",
      website: "",
      location: "",
      status: "",
      skills: "",
      githubusername: "",
      bio: "",
      twitter: "",
      facebook: "",
      linkedin: "",
      youtube: "",
      instagram: "",
      errors: {}
    };

    this.onChange = this.onChange.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.errors) {
      this.setState({
        errors: nextProps.errors
      });
    }
  }

  onSubmit(e) {
    e.preventDefault();

    const profileData = {
      handle: this.state.handle,
      company: this.state.company,
      website: this.state.website,
      location: this.state.location,
      status: this.state.status,
      skills: this.state.skills,
      githubusername: this.state.githubusername,
      bio: this.state.bio,
      twitter: this.state.twitter,
      facebook: this.state.facebook,
      linkedin: this.state.linkedin,
      youtube: this.state.youtube,
      instagram: this.state.instagram
    };

    this.props.createProfile(profileData, this.props.history);
  }

  onChange(e) {
    this.setState({
      [e.target.name]: e.target.value
    });
  }

  render() {
    const {
      handle,
      status,
      company,
      website,
      location,
      errors,
      displaySocialInput
    } = this.state;

    let socialInputs;

    if (displaySocialInput) {
      socialInputs = (
        <div>
          <InputGroup
            placeholder="Twitter profile URL"
            name="twitter"
            icon="fab fa-twitter"
            value={this.state.twitter}
            onChange={this.onChange}
            error={errors.twitter}
          />

          <InputGroup
            placeholder="Facebook profile URL"
            name="facebook"
            icon="fab fa-facebook"
            value={this.state.facebook}
            onChange={this.onChange}
            error={errors.facebook}
          />

          <InputGroup
            placeholder="Youtube profile URL"
            name="youtube"
            icon="fab fa-youtube"
            value={this.state.youtube}
            onChange={this.onChange}
            error={errors.youtube}
          />

          <InputGroup
            placeholder="Linkedin profile URL"
            name="linkedin"
            icon="fab fa-linkedin"
            value={this.state.linkedin}
            onChange={this.onChange}
            error={errors.linkedin}
          />

          <InputGroup
            placeholder="Instagram profile URL"
            name="instagram"
            icon="fab fa-instagram"
            value={this.state.instagram}
            onChange={this.onChange}
            error={errors.instagram}
          />
        </div>
      );
    }

    // Select options for status
    const options = [
      { label: "* Select Professional Status", value: 0 },
      { label: "Developer", value: "Developer" },
      { label: "Junior Developer", value: "Junior Developer" },
      { label: "Senior Developer", value: "Senior Developer" },
      { label: "Manager", value: "Manager" },
      { label: "Student", value: "Student" },
      { label: "Intern", value: "Intern" },
      { label: "Other", value: "Other" }
    ];

    return (
      <div className="create-profile">
        <div className="container">
          <div className="row">
            <div className="col-md-8 m-auto">
              <h1 className="display-4 text-center">Create your profile</h1>
              <p className="text-center">
                Let's get some information to make your profile look beautiful
              </p>
              <form onSubmit={this.onSubmit}>
                <TextFiledGroup
                  name="handle"
                  placeholder="* Desired profile handle"
                  value={handle}
                  error={errors.handle}
                  onChange={this.onChange}
                  info="A unique handle for your profile URL."
                />

                <SelectListGroup
                  name="status"
                  placeholder="Status"
                  value={status}
                  error={errors.status}
                  onChange={this.onChange}
                  options={options}
                  info="Give us an idea where you are at in your your carrier"
                />

                <TextFiledGroup
                  name="company"
                  placeholder="Company"
                  value={company}
                  error={errors.company}
                  onChange={this.onChange}
                  info="Could be your own company or one you work for"
                />

                <TextFiledGroup
                  name="website"
                  placeholder="Website"
                  value={website}
                  error={errors.website}
                  onChange={this.onChange}
                  info="Could be your own website or a company one"
                />

                <TextFiledGroup
                  name="location"
                  placeholder="Location"
                  value={location}
                  error={errors.location}
                  onChange={this.onChange}
                  info="City or city & state"
                />

                <TextFiledGroup
                  name="skills"
                  placeholder="* Skills"
                  value={this.state.skills}
                  error={errors.skills}
                  onChange={this.onChange}
                  info="Please use comma seperated values (eg. HTML,CSS,JavaScript,PHP)"
                />

                <TextFiledGroup
                  name="githubusername"
                  placeholder="Github Username"
                  value={this.state.githubusername}
                  error={errors.githubusername}
                  onChange={this.onChange}
                  info="If you want your lates repositories and a github link, include your username"
                />

                <TextAreaFiledGroup
                  name="bio"
                  placeholder="Short Bio"
                  value={this.state.bio}
                  error={errors.bio}
                  onChange={this.onChange}
                  info="Tell us about yourself"
                />

                <div className="mb-3">
                  <button
                    type="button"
                    className="btn btn-light"
                    onClick={() => {
                      this.setState(prevState => ({
                        displaySocialInput: !prevState.displaySocialInput
                      }));
                    }}
                  >
                    Add social network link
                  </button>
                  <div className="text-muted">Optional</div>
                </div>
                {socialInputs}
                <input
                  type="submit"
                  value="submit"
                  className="btn btn-info btn-block mt-4"
                />
              </form>
            </div>
          </div>
        </div>
        <form />
      </div>
    );
  }
}

CreateProfile.propTypes = {
  profile: PropTypes.object.isRequired,
  errors: PropTypes.object.isRequired
};

const mapStateToProps = state => ({
  profile: state.profile,
  errors: state.errors
});

export default connect(
  mapStateToProps,
  { createProfile }
)(withRouter(CreateProfile));
