const express = require("express");
const router = express.Router();
const mongoose = require("mongoose");
const passport = require("passport");

// Load Post Model
const Post = require("../../models/Post");
// Load Validator
const validatePostInput = require("../../validation/post");
// Load profile model
const profile = require("../../models/Profile");

// route    >>  GET api/posts/test
// desc     >>  Test post route
// access   >>  Public
router.get("/test", (req, res) => res.json({ msg: "Posts Works" }));

// route    >>  GET api/posts
// desc     >>  GET all Posts
// access   >>  Public
router.get("/", (req, res) => {
  Post.find()
    .sort({ date: -1 })
    .then(posts => res.json(posts))
    .catch(err => res.status(404).json({ nopost: "No posts found" }));
});

// route    >>  GET api/posts/:id
// desc     >>  GET Post by id
// access   >>  Public
router.get("/:id", (req, res) => {
  Post.findById(req.params.id)
    .then(post => res.json(post))
    .catch(err => res.status(404).json({ nopost: "No post found" }));
});

// route    >>  POST api/posts
// desc     >>  Create Post
// access   >>  Private
router.post(
  "/",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    const { errors, isValid } = validatePostInput(req.body);

    // Check validation
    if (!isValid) {
      return res.status(400).json(errors);
    }

    const newPost = new Post({
      text: req.body.text,
      name: req.body.name,
      avatar: req.body.avatar,
      user: req.user.id
    });

    newPost.save().then(post => res.json(post));
  }
);

// route    >>  DELETE api/posts/:id
// desc     >>  DELETE Post
// access   >>  Private
router.delete(
  "/:id",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    Profile.findOne({ user: req.user.id }).then(profile => {
      Post.findById(req.params.id)
        .then(post => {
          // Check for post owner
          if (post.user.toString() !== req.user.id) {
            return res
              .status(401)
              .json({ notauthorized: "User not authorized" });
          }

          // delete
          post.delete().then(() => res.json({ success: true }));
        })
        .catch(err => res.status(404).json({ nopost: "No post found" }));
    });
  }
);

// route    >>  POST api/posts/like/:id
// desc     >>  Like Post
// access   >>  Private
router.post(
  "/like/:id",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    Profile.findOne({ user: req.user.id }).then(profile => {
      Post.findById(req.params.id)
        .then(post => {
          if (
            post.likes.filter(like => like.user.toString() === req.user.id)
              .length > 0
          ) {
            return res
              .status(400)
              .json({ alreadyliked: "Already liked this post" });
          }

          // Add user to like array
          post.likes.unshift({ user: req.user.id });

          // Save to Database
          post.save().then(post => res.json(post));
        })
        .catch(err => res.status(404).json({ nopost: "No post found" }));
    });
  }
);

// route    >>  POST api/posts/unlike/:id
// desc     >>  Dislike Post
// access   >>  Private
router.post(
  "/unlike/:id",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    Profile.findOne({ user: req.user.id }).then(profile => {
      Post.findById(req.params.id)
        .then(post => {
          if (
            post.likes.filter(like => like.user.toString() === req.user.id)
              .length == 0
          ) {
            return res
              .status(400)
              .json({ notliked: "you have not liked this post" });
          }

          // Get remove index
          const removeIndex = post.likes
            .map(item => item.user.toString())
            .indexOf(req.user.id);

          // Splice out of array
          post.likes.splice(removeIndex, 1);

          // Save to Database
          post.save().then(post => res.json(post));
        })
        .catch(err => res.status(404).json({ nopost: "No post found" }));
    });
  }
);

// route    >>  POST api/posts/comment/:id
// desc     >>  Add comment to Post
// access   >>  Private
router.post(
  "/comment/:id",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    const { errors, isValid } = validatePostInput(req.body);

    // Check validation
    if (!isValid) {
      return res.status(400).json(errors);
    }
    Post.findById(req.params.id)
      .then(post => {
        const newComment = {
          text: req.body.text,
          name: req.body.name,
          avatar: req.body.avatar,
          user: req.user.id
        };

        // Add to comment array
        post.comments.unshift(newComment);

        // Save
        post.save().then(post => res.json(post));
      })
      .catch(err => res.status(404).json({ nopost: "No post found" }));
  }
);

// route    >>  DELETE api/posts/comment/:id/:comment_id
// desc     >>  Delete comment from Post
// access   >>  Private
router.delete(
  "/comment/:id/:comment_id",
  passport.authenticate("jwt", { session: false }),
  (req, res) => {
    Post.findById(req.params.id)
      .then(post => {
        // Check if comment exists
        if (
          post.comments.filter(
            comment => comment._id.toString() === req.params.comment_id
          ).length === 0
        ) {
          return res.status(404).json({ nocomment: "Comment does not exist" });
        }

        // Get remove index
        const removeIndex = post.comments
          .map(item => item._id.toString())
          .indexOf(req.params.comment_id);

        // Splice comment out of array
        post.comments.splice(removeIndex, 1);

        // Save to Database
        post.save().then(post => res.json(post));
      })
      .catch(err => res.status(404).json({ nocomment: "No Comment found" }));
  }
);

module.exports = router;
