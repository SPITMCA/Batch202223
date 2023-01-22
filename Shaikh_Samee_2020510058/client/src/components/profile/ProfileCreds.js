import React, { Component } from "react";
import Moment from "react-moment";

class ProfileCreds extends Component {
  render() {
    const { education, experience } = this.props;

    const expItem = experience.map(exp => (
      <li key={exp._id} className="list-group-item">
        <h4>{exp.company}</h4>
        <p>
          {<Moment format="DD/MM/YYYY">{exp.from}</Moment>} -{" "}
          {exp.to === null ? (
            "Current"
          ) : (
            <Moment format="DD/MM/YYYY">{exp.to}</Moment>
          )}
        </p>
        <p>
          <strong>Position: </strong> {exp.title}
        </p>

        <p>
          <strong>Location: </strong> {exp.location}
        </p>
        <p>
          <strong>Description: </strong> {exp.description}
        </p>
      </li>
    ));

    const eduItem = education.map(edu => (
      <li key={edu._id} className="list-group-item">
        <h4>{edu.school}</h4>
        <p>
          {<Moment format="DD/MM/YYYY">{edu.from}</Moment>} -{" "}
          {edu.to === null ? (
            "Current"
          ) : (
            <Moment format="DD/MM/YYYY">{edu.to}</Moment>
          )}
        </p>
        <p>
          <strong>Degree: </strong> {edu.degree}
        </p>
        <p>
          <strong>Field Of Study: </strong> {edu.fieldofstudy}
        </p>
        <p>
          <strong>Description:</strong> {edu.description}
        </p>
      </li>
    ));

    return (
      <div className="row">
        <div className="col-md-6">
          <h3 className="text-center text-info">Experience</h3>
          {expItem.length > 0 ? (
            <ul className="list-group">{expItem}</ul>
          ) : (
            <ul className="text-center">No Experience Listed</ul>
          )}
        </div>

        <div className="col-md-6">
          <h3 className="text-center text-info">Education</h3>
          {eduItem.length > 0 ? (
            <ul className="list-group">{eduItem}</ul>
          ) : (
            <ul className="text-center">No Education Listed</ul>
          )}
        </div>
      </div>
    );
  }
}

export default ProfileCreds;
